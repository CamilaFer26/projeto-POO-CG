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

// Handle nativo da janela (obtido do Canvas Java via JAWT) e contexto OpenGL
HWND hwnd = nullptr;
HDC hdc = nullptr;
HGLRC hglrc = nullptr;

// Controla o loop de renderizacao. Como nao ha mais GLFWwindow, quem derruba
// o loop e uma chamada externa a cleanup() (ex: ao fechar o JFrame).
volatile bool running = false;

float matA = 1.0f, matB = 0.0f, matC = 0.0f, matD = 1.0f;

// Limite do plano cartesiano: de -PLANE_LIMIT a +PLANE_LIMIT em X e Y
const float PLANE_LIMIT = 5.0f;

GLuint shaderProgram = 0;
GLint uniformTransformLoc;
GLint uniformColorLoc;
GLint uniformAspectLoc;
float aspectX = 1.0f;
float aspectY = 1.0f;

// Figura (o quadrado), definida em unidades CARTESIANAS (nao em pixels/clip space)
GLuint squareVAO = 0, squareVBO = 0;

// Grade do plano (linhas de -5 a 5)
GLuint gridVAO = 0, gridVBO = 0;
int gridVertexCount = 0;

// Eixos X e Y, desenhados em cores diferentes
GLuint axisVAO = 0, axisVBO = 0;

// Numeros do plano (labels -5 a 5 nos eixos, feitos com "digitos de 7 segmentos")
GLuint numbersVAO = 0, numbersVBO = 0;
int numbersVertexCount = 0;

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
    uniformAspectLoc = glGetUniformLocation(shaderProgram, "aspect"); // <-- adicionar

}

void setupSquare() {
    float vertices[] = {
        -1.0f, -1.0f,
         1.0f, -1.0f,
         1.0f,  1.0f,
        -1.0f,  1.0f
    };

    glGenVertexArrays(1, &squareVAO);
    glGenBuffers(1, &squareVBO);

    glBindVertexArray(squareVAO);
    glBindBuffer(GL_ARRAY_BUFFER, squareVBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);

    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(float), (void*)0);
    glEnableVertexAttribArray(0);

    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindVertexArray(0);
}

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

void setupAxis() {
    float axisVertices[] = {
        -PLANE_LIMIT, 0.0f,   PLANE_LIMIT, 0.0f,
        0.0f, -PLANE_LIMIT,   0.0f, PLANE_LIMIT
    };

    glGenVertexArrays(1, &axisVAO);
    glGenBuffers(1, &axisVBO);

    glBindVertexArray(axisVAO);
    glBindBuffer(GL_ARRAY_BUFFER, axisVBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(axisVertices), axisVertices, GL_STATIC_DRAW);

    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(float), (void*)0);
    glEnableVertexAttribArray(0);

    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindVertexArray(0);
}

// Mapa de segmentos ligados por digito (A,B,C,D,E,F,G), estilo display de 7 segmentos.
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

    numbersVertexCount = (int)(numberVertices.size() / 2);

    glGenVertexArrays(1, &numbersVAO);
    glGenBuffers(1, &numbersVBO);

    glBindVertexArray(numbersVAO);
    glBindBuffer(GL_ARRAY_BUFFER, numbersVBO);
    glBufferData(GL_ARRAY_BUFFER, numberVertices.size() * sizeof(float), numberVertices.data(), GL_STATIC_DRAW);

    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(float), (void*)0);
    glEnableVertexAttribArray(0);

    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindVertexArray(0);
}

void setupGraphics() {
    setupShader();
    setupSquare();
    setupGrid();
    setupAxis();
    setupNumbers();
}

const float IDENTITY_MATRIX[] = {
    1.0f, 0.0f, 0.0f,
    0.0f, 1.0f, 0.0f,
    0.0f, 0.0f, 1.0f
};

void renderScene() {
    glClear(GL_COLOR_BUFFER_BIT);
    glUseProgram(shaderProgram);
    glUniform2f(uniformAspectLoc, aspectX, aspectY);

    glUniformMatrix3fv(uniformTransformLoc, 1, GL_FALSE, IDENTITY_MATRIX);
    glUniform4f(uniformColorLoc, 0.35f, 0.35f, 0.35f, 1.0f);
    glBindVertexArray(gridVAO);
    glDrawArrays(GL_LINES, 0, gridVertexCount);

    glBindVertexArray(axisVAO);
    glUniform4f(uniformColorLoc, 0.85f, 0.25f, 0.25f, 1.0f);
    glDrawArrays(GL_LINES, 0, 2);
    glUniform4f(uniformColorLoc, 0.25f, 0.85f, 0.25f, 1.0f);
    glDrawArrays(GL_LINES, 2, 2);

    // Numeros (labels dos eixos)
    glUniform4f(uniformColorLoc, 0.9f, 0.9f, 0.9f, 1.0f);
    glBindVertexArray(numbersVAO);
    glDrawArrays(GL_LINES, 0, numbersVertexCount);

    float transform[] = {
        matA, matC, 0.0f,
        matB, matD, 0.0f,
        0.0f, 0.0f, 1.0f
    };

    glUniformMatrix3fv(uniformTransformLoc, 1, GL_FALSE, transform);
    glBindVertexArray(squareVAO);

    glUniform4f(uniformColorLoc, 0.3f, 0.6f, 0.9f, 0.35f);
    glDrawArrays(GL_TRIANGLE_FAN, 0, 4);

    glUniform4f(uniformColorLoc, 0.6f, 0.85f, 1.0f, 1.0f);
    glDrawArrays(GL_LINE_LOOP, 0, 4);

    // Antes: glfwSwapBuffers(window) + glfwPollEvents()
    // Agora o contexto e o HDC do Canvas Java, entao usamos a API do Windows direto
    SwapBuffers(hdc);
}

extern "C" {

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
    while (running) {
        renderScene();
    }
}

JNIEXPORT void JNICALL Java_graphics_MotorGrafico_update(JNIEnv*, jobject, jfloat a, jfloat b, jfloat c, jfloat d) {
    matA = a;
    matB = b;
    matC = c;
    matD = d;
}

JNIEXPORT void JNICALL Java_graphics_MotorGrafico_cleanup(JNIEnv*, jobject) {
    // Derruba o loop de renderizacao em init()
    running = false;

    if (squareVAO != 0) { glDeleteVertexArrays(1, &squareVAO); squareVAO = 0; }
    if (squareVBO != 0) { glDeleteBuffers(1, &squareVBO); squareVBO = 0; }

    if (gridVAO != 0) { glDeleteVertexArrays(1, &gridVAO); gridVAO = 0; }
    if (gridVBO != 0) { glDeleteBuffers(1, &gridVBO); gridVBO = 0; }

    if (axisVAO != 0) { glDeleteVertexArrays(1, &axisVAO); axisVAO = 0; }
    if (axisVBO != 0) { glDeleteBuffers(1, &axisVBO); axisVBO = 0; }

    if (numbersVAO != 0) { glDeleteVertexArrays(1, &numbersVAO); numbersVAO = 0; }
    if (numbersVBO != 0) { glDeleteBuffers(1, &numbersVBO); numbersVBO = 0; }

    if (shaderProgram != 0) { glDeleteProgram(shaderProgram); shaderProgram = 0; }

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

JNIEXPORT void JNICALL Java_graphics_MotorGrafico_shape(JNIEnv*, jobject, jint shape) {
	/* 0 -> quadrado
	 * 1 -> triângulo
	 * 2 -> retângulo
	*/
}

} // extern "C"