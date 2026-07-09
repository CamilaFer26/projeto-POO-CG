#include <jni.h>
#include <GL/glew.h>
#include <windows.h>
#include <jawt.h>
#include <jawt_md.h>
#include <cmath>
#include <iostream>
#include <vector>
#include <algorithm>
#include "../headers/graphics_MotorGrafico.h"
// ============================================================
// Janela e contexto OpenGL
// ============================================================
HWND hwnd = nullptr;   // handle nativo da janela por tras do Canvas Java (obtido via JAWT)
HDC hdc = nullptr;     // device context associado ao hwnd; usado para SwapBuffers a cada frame
HGLRC hglrc = nullptr; // contexto de renderizacao OpenGL, "current" apenas na glThread

// ============================================================
// Controle do loop de renderizacao
// ============================================================
volatile bool running = false; // true enquanto o loop em init() deve continuar rodando; cleanup() seta false para encerra-lo

// ============================================================
// Matriz de transformacao linear (2x2), controlada pela UI Java
// ============================================================
// | matrixM11  matrixM12 |
// | matrixM21  matrixM22 |
// Comeca como identidade (sem transformacao). Atualizada via update(), lida a cada frame em renderScene().
float matrixM11 = 1.0f, matrixM12 = 0.0f, matrixM21 = 0.0f, matrixM22 = 1.0f;

// ============================================================
// Plano cartesiano (grade, eixos, rotulos numericos)
// ============================================================
const float PLANE_LIMIT = 5.0f; // alcance do plano: de -5 a +5 em X e Y (usado para gerar grade, eixos e rotulos)
int gridVertexCount = 0;         // quantidade de vertices da grade, calculada em setupGrid()
GLuint gridVAO = 0, gridVBO = 0; // buffers das linhas da grade (fixas, GL_STATIC_DRAW)
GLuint axesVAO = 0, axesVBO = 0; // buffers dos eixos X e Y (2 segmentos fixos, 4 vertices no total)
GLuint axisLabelsVAO = 0, axisLabelsVBO = 0; // buffers dos numeros desenhados nos eixos (estilo 7 segmentos)
int axisLabelsVertexCount = 0;                // total de vertices de todos os rotulos combinados, calculado em setupNumbers()

// ============================================================
// Forma geometrica ativa (quadrado / triangulo / circulo / vetor)
// ============================================================
GLuint shapeVao = 0, shapeVBO = 0; // buffers da forma atualmente exibida; conteudo trocado dinamicamente via updateShapeBuffer()
int shapeVertexCount = 4;          // quantidade de vertices da forma atual
GLenum shapeDrawMode = GL_LINE_LOOP; // modo usado no contorno/linha; TRIANGLE_FAN so roda se shapeFilled=true
bool shapeFilled = true;             // false = sem preenchimento (caso do vetor)

// Vetor atual (usado quando shape == 3), atualizado via vector()
float vectorX = 1.0f, vectorY = 0.0f;

// Mecanismo de "estado pendente" para trocar de forma com seguranca entre threads:
// a EDT (Java) so escreve aqui via shape()/vector(); quem aplica de fato e a glThread, dentro de renderScene()
volatile int requestShapeId = 0;    // id da forma pedida (0=quadrado, 1=triangulo, 2=circulo, 3=vetor), ainda nao aplicada
volatile bool shapeChanged = false; // sinaliza que existe uma troca de forma pendente para renderScene() processar

// ============================================================
// Shader program e uniforms
// ============================================================
GLuint shaderProgram = 0;        // programa de shader compilado/linkado (vertex + fragment)
GLint uniformTransformLoc;       // localizacao do uniform "transform" (mat3) no shader
GLint uniformColorLoc;           // localizacao do uniform "color" (vec4) no shader
GLint uniformAspectLoc;          // localizacao do uniform "aspect" (vec2) no shader

// ============================================================
// Correcao de aspect ratio (canvas nao-quadrado)
// ============================================================
float aspectX = 1.0f; // fator de correcao no eixo X, calculado em init() a partir das dimensoes reais do canvas
float aspectY = 1.0f; // fator de correcao no eixo Y, idem

// ============================================================
// Shaders: codigo-fonte GLSL
// ============================================================

const char* vertexShaderSource = R"(
        #version 330 core
        layout (location = 0) in vec2 aPos;
        uniform mat3 transform;
        uniform vec2 aspect;

        void main()
        {
            // Converte unidades cartesianas (-5..5) para o clip space do OpenGL (-1..1)
            const float worldScale = 1.0 / 5.0;

            vec3 pos = transform * vec3(aPos, 1.0);
            pos.xy *= worldScale;
            pos.xy *= aspect; // corrige a distorcao de canvas nao-quadrado
            gl_Position = vec4(pos.xy, 0.0, 1.0);
        }
    )";

const char* fragmentShaderSource = R"(
        #version 330 core
        out vec4 FragColor;
        uniform vec4 color;

        void main()
        {
            FragColor = color;
        }
    )";

// ============================================================
// Shaders: compilacao e linkagem
// ============================================================

// Compila um unico shader (vertex ou fragment) a partir do source, checando erros de compilacao
GLuint compileShader(GLenum type, const char* source) {
    GLuint shader = glCreateShader(type);
    glShaderSource(shader, 1, &source, NULL);
    glCompileShader(shader);

    int success;
    glGetShaderiv(shader, GL_COMPILE_STATUS, &success);
    if (!success) {
        char infoLog[512];
        glGetShaderInfoLog(shader, 512, NULL, infoLog);
        std::cout << "Erro ao compilar shader:\n" << infoLog << std::endl;
    }
    return shader;
}

// Compila vertex+fragment, linka no shaderProgram e cacheia as localizacoes dos uniforms
void setupShader() {
    GLuint vertexShader = compileShader(GL_VERTEX_SHADER, vertexShaderSource);
    GLuint fragmentShader = compileShader(GL_FRAGMENT_SHADER, fragmentShaderSource);

    shaderProgram = glCreateProgram();
    glAttachShader(shaderProgram, vertexShader);
    glAttachShader(shaderProgram, fragmentShader);
    glLinkProgram(shaderProgram);

    int success;
    glGetProgramiv(shaderProgram, GL_LINK_STATUS, &success);
    if (!success) {
        char infoLog[512];
        glGetProgramInfoLog(shaderProgram, 512, NULL, infoLog);
        std::cout << "Erro ao linkar shader program:\n" << infoLog << std::endl;
    }

    glDeleteShader(vertexShader);
    glDeleteShader(fragmentShader);

    uniformTransformLoc = glGetUniformLocation(shaderProgram, "transform");
    uniformColorLoc = glGetUniformLocation(shaderProgram, "color");
    uniformAspectLoc = glGetUniformLocation(shaderProgram, "aspect");
}

// ============================================================
// Forma geometrica ativa (quadrado / triangulo / circulo / vetor)
// ============================================================

// Protótipo: precisa existir antes de updateShapeBuffer() usá-la (definição completa vem mais abaixo)
std::vector<float> buildVectorVertices(float x, float y);

// Reescreve o conteudo do shapeVBO com os vertices da forma pedida (0=quadrado, 1=triangulo, 2=circulo, 3=vetor)
// Chamada tanto na inicializacao quanto sempre que shapeChanged sinaliza uma troca pendente
void updateShapeBuffer(int shape) {
    std::vector<float> vertices;

    switch (shape) {
        case 1: // triangulo
            vertices = { 0.0f, 1.0f,  -1.0f, -1.0f,  1.0f, -1.0f };
            shapeDrawMode = GL_LINE_LOOP;
            shapeFilled = true;
            break;
        case 2: { // circulo
            const int numSegmentos = 64;
            const float raio = 1.0f;
            for (int i = 0; i < numSegmentos; i++) {
                float angulo = 2.0f * 3.14159265f * (float)i / (float)numSegmentos;
                vertices.push_back(raio * cosf(angulo));
                vertices.push_back(raio * sinf(angulo));
            }
            shapeDrawMode = GL_LINE_LOOP;
            shapeFilled = true;
            break;
        }
        case 3: // vetor
            vertices = buildVectorVertices(vectorX, vectorY);
            shapeDrawMode = GL_LINES;
            shapeFilled = false;
            break;
        case 0:
        default: // quadrado
            vertices = { -1.0f, -1.0f,  1.0f, -1.0f,  1.0f, 1.0f,  -1.0f, 1.0f };
            shapeDrawMode = GL_LINE_LOOP;
            shapeFilled = true;
            break;
    }

    shapeVertexCount = (int)(vertices.size() / 2);

    glBindVertexArray(shapeVao);
    glBindBuffer(GL_ARRAY_BUFFER, shapeVBO);
    glBufferData(GL_ARRAY_BUFFER, vertices.size() * sizeof(float), vertices.data(), GL_DYNAMIC_DRAW);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindVertexArray(0);
}

// Gera os vertices de um vetor (linha + ponta de seta) partindo da origem
std::vector<float> buildVectorVertices(float x, float y) {
    std::vector<float> v;

    // linha principal: origem -> ponta
    v.push_back(0.0f); v.push_back(0.0f);
    v.push_back(x);    v.push_back(y);

    // cabeca da seta: calcula o angulo do vetor
    float comprimento = sqrtf(x * x + y * y);
    if (comprimento < 0.0001f) return v; // vetor nulo, sem seta

    float angulo = atan2f(y, x);
    const float tamanhoSeta = 0.12f;      // tamanho das "asas" da seta
    const float aberturaSeta = 0.4f;      // abertura angular (radianos)

    float ang1 = angulo + 3.14159265f - aberturaSeta;
    float ang2 = angulo + 3.14159265f + aberturaSeta;

    // asa 1: ponta -> lado esquerdo
    v.push_back(x); v.push_back(y);
    v.push_back(x + tamanhoSeta * cosf(ang1));
    v.push_back(y + tamanhoSeta * sinf(ang1));

    // asa 2: ponta -> lado direito
    v.push_back(x); v.push_back(y);
    v.push_back(x + tamanhoSeta * cosf(ang2));
    v.push_back(y + tamanhoSeta * sinf(ang2));

    return v;
}

// Cria o VAO/VBO da forma geometrica (uma unica vez) e inicializa como quadrado
void setupShape() {
    glGenVertexArrays(1, &shapeVao);
    glGenBuffers(1, &shapeVBO);

    glBindVertexArray(shapeVao);
    glBindBuffer(GL_ARRAY_BUFFER, shapeVBO);

    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(float), (void*)0);
    glEnableVertexAttribArray(0);

    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindVertexArray(0);

    updateShapeBuffer(0); // comeca como quadrado, igual ao comportamento original
}

// ============================================================
// Plano cartesiano: grade e eixos (geometria fixa, GL_STATIC_DRAW)
// ============================================================

// Gera as linhas da grade (uma vertical e uma horizontal por unidade, de -PLANE_LIMIT a +PLANE_LIMIT)
void setupGrid() {
    std::vector<float> gridVertices;

    int limit = (int)PLANE_LIMIT;
    for (int i = -limit; i <= limit; i++) {
        gridVertices.push_back((float)i);
        gridVertices.push_back(-PLANE_LIMIT);
        gridVertices.push_back((float)i);
        gridVertices.push_back(PLANE_LIMIT);

        gridVertices.push_back(-PLANE_LIMIT);
        gridVertices.push_back((float)i);
        gridVertices.push_back(PLANE_LIMIT);
        gridVertices.push_back((float)i);
    }

    gridVertexCount = (int)(gridVertices.size() / 2);

    glGenVertexArrays(1, &gridVAO);
    glGenBuffers(1, &gridVBO);

    glBindVertexArray(gridVAO);
    glBindBuffer(GL_ARRAY_BUFFER, gridVBO);
    glBufferData(GL_ARRAY_BUFFER, gridVertices.size() * sizeof(float), gridVertices.data(), GL_STATIC_DRAW);

    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(float), (void*)0);
    glEnableVertexAttribArray(0);

    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindVertexArray(0);
}

// Gera os dois segmentos que formam os eixos X e Y (fixos, de -PLANE_LIMIT a +PLANE_LIMIT)
void setupAxis() {
    float axisVertices[] = {
        -PLANE_LIMIT, 0.0f,   PLANE_LIMIT, 0.0f,
        0.0f, -PLANE_LIMIT,   0.0f, PLANE_LIMIT
    };

    glGenVertexArrays(1, &axesVAO);
    glGenBuffers(1, &axesVBO);

    glBindVertexArray(axesVAO);
    glBindBuffer(GL_ARRAY_BUFFER, axesVBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(axisVertices), axisVertices, GL_STATIC_DRAW);

    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(float), (void*)0);
    glEnableVertexAttribArray(0);

    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindVertexArray(0);
}
// ============================================================
// Rotulos numericos dos eixos (digitos estilo "7 segmentos")
// ============================================================

// Tabela de lookup: para cada digito (0-9) ou o sinal de menos (indice 10),
// diz quais dos 7 segmentos (A-G) estao acesos.
// A=topo, B=direita-cima, C=direita-baixo, D=base, E=esquerda-baixo, F=esquerda-cima, G=meio
bool segmentsOn[11][7] = {
    /*0*/ {1,1,1,1,1,1,0},
    /*1*/ {0,1,1,0,0,0,0},
    /*2*/ {1,1,0,1,1,0,1},
    /*3*/ {1,1,1,1,0,0,1},
    /*4*/ {0,1,1,0,0,1,1},
    /*5*/ {1,0,1,1,0,1,1},
    /*6*/ {1,0,1,1,1,1,1},
    /*7*/ {1,1,1,0,0,0,0},
    /*8*/ {1,1,1,1,1,1,1},
    /*9*/ {1,1,1,1,0,1,1},
    /*10 = sinal de menos*/ {0,0,0,0,0,0,1}
};

// Adiciona os segmentos de UM digito (0-9, ou 10 para "-") na posicao (x,y),
// com o tamanho controlado por scale. x,y sao o canto inferior-esquerdo do digito.
void addDigit(std::vector<float>& verts, int digit, float x, float y, float scale) {
    float w = 0.5f * scale;
    float h = 1.0f * scale;
    float hw = 0.5f * h; // altura da metade (usada no meio do digito)

    float segPoints[7][4] = {
        {0.0f, h,    w, h   },  // A: topo
        {w,    h,    w, hw  },  // B: direita-cima
        {w,    hw,   w, 0.0f},  // C: direita-baixo
        {0.0f, 0.0f, w, 0.0f},  // D: base
        {0.0f, 0.0f, 0.0f, hw}, // E: esquerda-baixo
        {0.0f, hw,   0.0f, h},  // F: esquerda-cima
        {0.0f, hw,   w, hw  }   // G: meio
    };

    for (int s = 0; s < 7; s++) {
        if (!segmentsOn[digit][s]) continue;
        verts.push_back(x + segPoints[s][0]);
        verts.push_back(y + segPoints[s][1]);
        verts.push_back(x + segPoints[s][2]);
        verts.push_back(y + segPoints[s][3]);
    }
}

// Adiciona um numero inteiro (pode ser negativo) na posicao (x,y), centralizado
// horizontalmente. gap eh o espaco entre digitos.
void addNumber(std::vector<float>& verts, int number, float centerX, float y, float scale) {
    bool negative = number < 0;
    int value = std::abs(number);

    std::vector<int> digits;
    if (value == 0) {
        digits.push_back(0);
    } else {
        while (value > 0) {
            digits.push_back(value % 10);
            value /= 10;
        }
        std::reverse(digits.begin(), digits.end());
    }

    float digitWidth = 0.5f * scale;
    float gap = 0.15f * scale;
    int glyphCount = (int)digits.size() + (negative ? 1 : 0);
    float totalWidth = glyphCount * digitWidth + (glyphCount - 1) * gap;

    float cursorX = centerX - totalWidth / 2.0f;

    if (negative) {
        addDigit(verts, 10, cursorX, y, scale); // "-"
        cursorX += digitWidth + gap;
    }
    for (int d : digits) {
        addDigit(verts, d, cursorX, y, scale);
        cursorX += digitWidth + gap;
    }
}

// Gera todos os rotulos numericos dos eixos X e Y (de -PLANE_LIMIT a +PLANE_LIMIT) num unico buffer
void setupNumbers() {
    std::vector<float> numberVertices;

    int limit = (int)PLANE_LIMIT;
    float digitScale = 0.28f;

    for (int i = -limit; i <= limit; i++) {
        // Numeros do eixo X: um por coluna, incluindo o 0 (fica sob o eixo Y, igual aos outros)
        addNumber(numberVertices, i, (float)i, -0.35f, digitScale);

        // Numeros do eixo Y: um por linha, exceto 0 (ja coberto pelo label do eixo X na origem)
        if (i != 0) {
            addNumber(numberVertices, i, -0.65f, (float)i - 0.14f, digitScale);
        }
    }

    axisLabelsVertexCount = (int)(numberVertices.size() / 2);

    glGenVertexArrays(1, &axisLabelsVAO);
    glGenBuffers(1, &axisLabelsVBO);

    glBindVertexArray(axisLabelsVAO);
    glBindBuffer(GL_ARRAY_BUFFER, axisLabelsVBO);
    glBufferData(GL_ARRAY_BUFFER, numberVertices.size() * sizeof(float), numberVertices.data(), GL_STATIC_DRAW);

    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(float), (void*)0);
    glEnableVertexAttribArray(0);

    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindVertexArray(0);
}

// ============================================================
// Inicializacao conjunta de todos os objetos graficos
// ============================================================

// Chamada uma vez em init(), apos o contexto OpenGL estar current
void setupGraphics() {
    setupShader();
    setupShape();
    setupGrid();
    setupAxis();
    setupNumbers();
}

// ============================================================
// Loop de renderizacao (roda continuamente na glThread)
// ============================================================

const float IDENTITY_MATRIX[] = {
    1.0f, 0.0f, 0.0f,
    0.0f, 1.0f, 0.0f,
    0.0f, 0.0f, 1.0f
};

// Desenha um frame completo: aplica troca de forma pendente, depois grade -> eixos -> rotulos -> forma
void renderScene() {
    if (shapeChanged) {
        updateShapeBuffer(requestShapeId);
        shapeChanged = false;
    }

    glClear(GL_COLOR_BUFFER_BIT);
    glUseProgram(shaderProgram);
    glUniform2f(uniformAspectLoc, aspectX, aspectY);

    // --- grade (fixa, sem transformacao) ---
    glUniformMatrix3fv(uniformTransformLoc, 1, GL_FALSE, IDENTITY_MATRIX);
    glUniform4f(uniformColorLoc, 0.35f, 0.35f, 0.35f, 1.0f);
    glBindVertexArray(gridVAO);
    glDrawArrays(GL_LINES, 0, gridVertexCount);

    // --- eixos X (vermelho) e Y (verde) ---
    glBindVertexArray(axesVAO);
    glUniform4f(uniformColorLoc, 0.85f, 0.25f, 0.25f, 1.0f);
    glDrawArrays(GL_LINES, 0, 2);
    glUniform4f(uniformColorLoc, 0.25f, 0.85f, 0.25f, 1.0f);
    glDrawArrays(GL_LINES, 2, 2);

    // --- rotulos numericos dos eixos ---
    glUniform4f(uniformColorLoc, 0.9f, 0.9f, 0.9f, 1.0f);
    glBindVertexArray(axisLabelsVAO);
    glDrawArrays(GL_LINES, 0, axisLabelsVertexCount);

    // --- forma geometrica (afetada pela matriz de transformacao do usuario) ---
    float transform[] = {
        matrixM11, matrixM21, 0.0f,
        matrixM12, matrixM22, 0.0f,
        0.0f, 0.0f, 1.0f
    };

    glUniformMatrix3fv(uniformTransformLoc, 1, GL_FALSE, transform);
    glBindVertexArray(shapeVao);

    if (shapeFilled) {
        glUniform4f(uniformColorLoc, 0.3f, 0.6f, 0.9f, 0.35f); // preenchimento semitransparente
        glDrawArrays(GL_TRIANGLE_FAN, 0, shapeVertexCount);
    }

    glUniform4f(uniformColorLoc, 0.6f, 0.85f, 1.0f, 1.0f); // contorno/linha opaca
    glDrawArrays(shapeDrawMode, 0, shapeVertexCount);

    SwapBuffers(hdc);
}

// ============================================================
// Funcoes JNI expostas ao Java (graphics.MotorGrafico)
// ============================================================

extern "C" {

    // Conecta ao Canvas via JAWT, cria o contexto OpenGL, inicializa os graficos
    // e entra no loop de renderizacao (nunca retorna ate running virar false)
    JNIEXPORT void JNICALL Java_graphics_MotorGrafico_init(JNIEnv* env, jobject obj, jobject canvas) {
        // 1) Obter o JAWT
        JAWT awt;
        awt.version = JAWT_VERSION_9;
        if (JAWT_GetAWT(env, &awt) == JNI_FALSE) {
            std::cout << "Erro ao obter JAWT" << std::endl;
            return;
        }

        // 2) Obter a DrawingSurface do Canvas e travar
        JAWT_DrawingSurface* ds = awt.GetDrawingSurface(env, canvas);
        if (ds == nullptr) {
            std::cout << "Erro ao obter DrawingSurface" << std::endl;
            return;
        }

        jint lock = ds->Lock(ds);
        if ((lock & JAWT_LOCK_ERROR) != 0) {
            std::cout << "Erro ao travar DrawingSurface" << std::endl;
            awt.FreeDrawingSurface(ds);
            return;
        }

        // 3) Extrair o HWND nativo do Canvas (Windows)
        JAWT_DrawingSurfaceInfo* dsi = ds->GetDrawingSurfaceInfo(ds);
        if (dsi == nullptr) {
            std::cout << "Erro ao obter DrawingSurfaceInfo" << std::endl;
            ds->Unlock(ds);
            awt.FreeDrawingSurface(ds);
            return;
        }

        JAWT_Win32DrawingSurfaceInfo* dsiWin = (JAWT_Win32DrawingSurfaceInfo*)dsi->platformInfo;
        hwnd = dsiWin->hwnd;

        ds->FreeDrawingSurfaceInfo(dsi);
        ds->Unlock(ds);
        awt.FreeDrawingSurface(ds);

        if (hwnd == nullptr) {
            std::cout << "HWND invalido" << std::endl;
            return;
        }

        // 4) Criar o contexto OpenGL em cima do HWND do Canvas
        hdc = GetDC(hwnd);

        PIXELFORMATDESCRIPTOR pfd = { sizeof(PIXELFORMATDESCRIPTOR), 1 };
        pfd.dwFlags = PFD_DRAW_TO_WINDOW | PFD_SUPPORT_OPENGL | PFD_DOUBLEBUFFER;
        pfd.iPixelType = PFD_TYPE_RGBA;
        pfd.cColorBits = 32;
        pfd.cDepthBits = 24;
        pfd.iLayerType = PFD_MAIN_PLANE;

        int pixelFormat = ChoosePixelFormat(hdc, &pfd);
        if (pixelFormat == 0 || !SetPixelFormat(hdc, pixelFormat, &pfd)) {
            std::cout << "Erro ao configurar pixel format" << std::endl;
            return;
        }

        hglrc = wglCreateContext(hdc);
        wglMakeCurrent(hdc, hglrc);

        glewExperimental = GL_TRUE;
        if (glewInit() != GLEW_OK) {
            std::cout << "Erro ao iniciar GLEW" << std::endl;
            return;
        }

        RECT rect;
        GetClientRect(hwnd, &rect);
        int width = rect.right - rect.left;
        int height = rect.bottom - rect.top;
        glViewport(0, 0, width, height);

        // Mantem proporcao 1:1 entre unidades do plano em X e Y, evitando esticar
        // o desenho quando o canvas nao e quadrado (ex: 750x550)
        int minDim = (width < height) ? width : height;
        aspectX = (float)minDim / (float)width;
        aspectY = (float)minDim / (float)height;

        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        setupGraphics();

        // 5) Loop de renderizacao. Termina quando cleanup() setar running = false
        running = true;
        while (running)
            renderScene();
    }

    // Recebe a nova matriz de transformacao linear vinda da UI Java e grava nas variaveis globais
    JNIEXPORT void JNICALL Java_graphics_MotorGrafico_update(JNIEnv*, jobject, jfloat a, jfloat b, jfloat c, jfloat d) {
        matrixM11 = a;
        matrixM12 = b;
        matrixM21 = c;
        matrixM22 = d;
    }

    // Encerra o loop de renderizacao e libera todos os recursos GPU/janela/contexto
    JNIEXPORT void JNICALL Java_graphics_MotorGrafico_cleanup(JNIEnv*, jobject) {
        // Derruba o loop de renderizacao em init()
        running = false;

        if (shapeVao != 0)
            glDeleteVertexArrays(1, &shapeVao); shapeVao = 0; 
        if (shapeVBO != 0)
            glDeleteBuffers(1, &shapeVBO); shapeVBO = 0; 

        if (gridVAO != 0) 
            glDeleteVertexArrays(1, &gridVAO); gridVAO = 0; 
        if (gridVBO != 0) 
            glDeleteBuffers(1, &gridVBO); gridVBO = 0; 

        if (axesVAO != 0) 
            glDeleteVertexArrays(1, &axesVAO); axesVAO = 0; 
        if (axesVBO != 0) 
            glDeleteBuffers(1, &axesVBO); axesVBO = 0; 

        if (axisLabelsVAO != 0) 
            glDeleteVertexArrays(1, &axisLabelsVAO); axisLabelsVAO = 0; 
        if (axisLabelsVBO != 0) 
            glDeleteBuffers(1, &axisLabelsVBO); axisLabelsVBO = 0; 

        if (shaderProgram != 0) 
            glDeleteProgram(shaderProgram); shaderProgram = 0; 

        if (hglrc != nullptr) {
            wglMakeCurrent(nullptr, nullptr);
            wglDeleteContext(hglrc);
            hglrc = nullptr;
        }
        if (hdc != nullptr && hwnd != nullptr) {
            ReleaseDC(hwnd, hdc);
            hdc = nullptr;
        }

        hwnd = nullptr;
    }

    // Recebe um pedido de troca de forma da UI Java; so grava estado, quem aplica e renderScene()
    JNIEXPORT void JNICALL Java_graphics_MotorGrafico_shape(JNIEnv*, jobject, jint shape) {
        requestShapeId = (int)shape;
        shapeChanged = true;
    }

    // Recebe um novo vetor (x,y) da UI Java; atualiza o vetor e pede troca para a forma 3 (vetor)
    JNIEXPORT void JNICALL Java_graphics_MotorGrafico_vector(JNIEnv*, jobject, jfloat x, jfloat y) {
        vectorX = x;
        vectorY = y;
        requestShapeId = 3;
        shapeChanged = true;
    }

}