# **Build a text editor in Java(CS61b Proj 2)** 

Overview
---
In this project, I built a text editor with Java FX library. It supports editing file, undo/redo, changing font size, word warping, window resizing, clicking and scrolling. I use a modified double linked list as the base data structure to store the text information. It supports O(1) insert/delete and O(N) rending. The main source files are located at Editor/Editor.java and Editor/FastLinkedList.java