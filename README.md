# Lodo

[![Join the chat at https://gitter.im/k3d3/lodo](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/k3d3/lodo?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Lodo is an application that uses a tree of lists to organize many projects and to-do lists.

The application is organized into notebooks, pages and lists. Notebooks are the top level and
are displayed at the left side of the window. Pages are contained within a notebook, and within
those pages are a number of lists, which can contain items or lists of their own.

The idea behind it is to store a tree of lists, each of which can be independently manipulated to
add and move lists and items around the tree.

Items and lists are the same thing - an item is merely an empty list. In fact, everything is a list,
including notebooks and pages. This allows you to start a deep list somewhere, and if needed, move it
into its own notebook or page without any modifications.

# Usage and Installation

There are two ways to run Lodo.

## Packaging
If you want to simply package it into a jar:

    ./bin/sbt assembly

SBT will then optimize and compile everything into a fat jar that can be deployed anywhere Java is run:

    java -jar lodo-assembly-0.0.1.jar

If you want to specify the running port, use the PORT environment variable:

    PORT=5001 java -jar lodo-assembly-0.0.1.jar


## Development
Alternatively, if you want to develop on Lodo:

    ./bin/sbt ~re-start

(if you're using zsh as a shell, you might need to use \\~ instead of ~)

This will compile the code in dev mode and run it. Any changes made to the source will make
the code recompile and will restart the server. It will also compile much faster.