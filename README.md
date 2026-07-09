# Projeto – Visualização de Transformações Lineares
Projeto de visualizador educacional de transformações lineares para as disciplinas de Programação Orientada a Objetos e Computação Gráfica.

## Requisitos do ambiente
O projeto foi desenvolvido e testado com o seguinte ambiente:

* Java JDK 21
* `javac 21.0.11`
* GCC (MSYS2)
  * `g++ (Rev2, Built by MSYS2 project) 14.2.0`
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
## Instalação das bibliotecas
Abra o terminal **MSYS2 MINGW64** e instale as dependências:

```bash
pacman -S mingw-w64-ucrt-x86_64-glfw
pacman -S mingw-w64-ucrt-x86_64-glew
```

Essas bibliotecas serão utilizadas pelo motor gráfico em C++.
---

## Estrutura do projeto
```text
Projeto/
│
├── build/
├── headers/
├── lib/
├── native/
│   └── motor.cpp
├── src/
│   ├── graphics/
│   ├── model/
│   └── view/
└── README.md
```

---
## Compilação
### 1. Compilar as classes Java e gerar os cabeçalhos JNI
```bash
javac -h headers -d build src/model/Matriz.java src/graphics/MotorGrafico.java src/view/Main.java src/view/TransformUI.java src/view/TelaInicial.java
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
## Executando novamente
Se nenhuma alteração foi feita no código Java ou C++, basta executar:
```bash
java -Djava.library.path=lib -cp build view.Main
```
Não é necessário recompilar o projeto.

---
## Quando recompilar?
### Alterações em arquivos Java
Execute novamente:
```bash
javac -h headers -d build ...
```

---
### Alterações em `motor.cpp`
Execute novamente:

```bash
g++ -shared ...
```

---
### Alterações na assinatura de métodos `native`

Exemplo:
```java
public native void update(...);
```

É necessário:
1. recompilar o Java para gerar um novo cabeçalho JNI;
2. recompilar a DLL.
Caso contrário ocorrerão erros de incompatibilidade entre Java e C++.

---
## Observações
* O projeto utiliza **JNI** para comunicação entre Java e C++.
* O motor gráfico é compilado como uma DLL (`motor.dll`).
* A interface é desenvolvida em Java (Swing).
* O motor gráfico utiliza OpenGL através das bibliotecas **GLEW** e **GLFW**.
* Recomenda-se que todos os integrantes do grupo utilizem a mesma versão do JDK, do MSYS2 e das bibliotecas para evitar incompatibilidades durante a compilação.

