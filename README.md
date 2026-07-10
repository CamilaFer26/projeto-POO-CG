# Projeto Interdisciplinar
## Visualizador Educacional de Transformações Lineares em 2D
#### __Projeto de visualizador educacional de transformações lineares para as disciplinas de Programação Orientada a Objetos e Computação Gráfica.__

## Sobre o projeto
Conceitos de Álgebra Linear normalmente são apresentados de maneira abstrata, dificultando a compreensão intuitiva de matrizes e transformações lineares. Muitos estudantes conseguem realizar operações algébricas, porém não compreendem o impacto visual das transformações sobre objetos no plano.

Tendo em vista essa problemática, o projeto propõe o desenvolvimento de uma ferramenta educacional interativa voltada ao ensino de Álgebra Linear. O sistema permite a visualização dinâmica de transformações lineares em objetos bidimensionais, explorando conceitos como rotação, escala, reflexão, composição de matrizes e interpretação geométrica do determinante.

A proposta segue integralmente a arquitetura definida para o projeto interdisciplinar, com separação em três camadas: aplicação Java, integração JNI e motor gráfico em C/C++ com OpenGL.

## Objetivos do projeto
- Disponibilizar uma plataforma educacional para visualização interativa de transformações lineares em 2D utilizando Java, JNI, C/C++ e OpenGL;
- Implementar transformações lineares em tempo real;
- Demonstrar visualmente os efeitos do determinante;
- Permitir manipulação interativa de matrizes;
- Integrar aplicação Java com motor gráfico em C/C++;
- Aplicar princípios de POO e arquitetura multicamadas;
- Utilizar OpenGL para renderização 2D.

## Estrutura do projeto
```text
Projeto/
│
├── headers/
├── lib/
├── native/
│   └── motor.cpp
├── src/
│   ├── controller/
│   ├── graphics/
│   ├── model/
│   └── view/
└── README.md
```

# Como utilizar o aplicativo?
Existem duas formas de executar o projeto:
* __**Desenvolvimento**__: clonar o repositório, configurar o ambiente e compilar o código-fonte conforme as instruções deste documento.
* __**Aplicação pronta**__: baixar a versão disponibilizada na seção Releases do repositório. Nesse caso, não é necessário instalar o JDK nem compilar o projeto.

## Requisitos do ambiente
O projeto foi desenvolvido e testado com o seguinte ambiente:

1. Java JDK 21 (olhar a seção 'Instalação do JDK')
* * `javac 21.0.11`
2. GCC (MSYS2) (olhar a seção 'instalçao do MSYS2')
*  * `g++ (Rev2, Built by MSYS2 project) 14.2.0`
* Windows 10/11
* MSYS2 utilizando o ambiente **MINGW64**

> **Importante:** utilize um **JDK** (não apenas o JRE), pois o projeto utiliza JNI (Java Native Interface).
---

## Instalação do JDK
Verifique a instalação:
```bash
javac --version
```
Resultado esperado:
```text
javac 21.0.11
```
Também confirme que a variável `JAVA_HOME` aponta para a instalação do JDK.
Exemplo:
```text
C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot
```

---
## Instalação do MSYS2
Instale o MSYS2 e utilize o ambiente **MINGW64**.
Verifique o compilador:
```bash
g++ --version
```
Resultado esperado:
```text
g++ (Rev2, Built by MSYS2 project) 14.2.0
```

---
### Instalação das bibliotecas
Abra o terminal **MSYS2 MINGW64** e instale as dependências:
```bash
pacman -S mingw-w64-x86_64-glfw
pacman -S mingw-w64-x86_64-glew
```

> **Importante:** Essas bibliotecas serão utilizadas pelo motor gráfico em C++.

---
## Compilação
### 1. Compilar as classes Java e gerar os cabeçalhos JNI
```bash
javac -h headers -d build src/**/*.java
```
Este comando:
* compila as classes Java;
* gera os arquivos de cabeçalho JNI na pasta `headers`;
* grava os arquivos `.class` na pasta `build`.

---
### 2. Compilar o motor gráfico em C++
```bash
g++ -shared -o lib/motor.dll native/motor.cpp ^
-Iheaders ^
-I"%JAVA_HOME%\include" ^
-I"%JAVA_HOME%\include\win32" ^
-I"C:\glew\include" ^
-I"C:\glfw\include" ^
-L"C:\glew\lib\Release\x64" ^
-L"C:\glfw\lib-mingw-w64" ^
-L"%JAVA_HOME%\lib" ^
-lglew32 ^
-lglfw3 ^
-lgdi32 ^
-luser32 ^
-lshell32 ^
-lopengl32 ^
-ljawt
```
> Esses caminhos devem ser adaptados conforme a instalação do usuário.

Esse comando gera a biblioteca nativa:
```text
lib/motor.dll
```

---
### 3. Executar o projeto
```bash
java -Djava.library.path=lib -cp build view.Main
```

O parâmetro
```text
-Djava.library.path=lib
```
informa ao Java onde localizar a biblioteca `motor.dll`.

---
## Observações
* O projeto utiliza **JNI** para comunicação entre Java e C++.
* O motor gráfico é compilado como uma DLL (`motor.dll`).
* A interface é desenvolvida em Java (Swing).
* O motor gráfico utiliza OpenGL através das bibliotecas **GLEW** e **GLFW**.
* Recomenda-se utilizar a mesma versão do JDK, do MSYS2 e das bibliotecas para evitar incompatibilidades durante a compilação.
