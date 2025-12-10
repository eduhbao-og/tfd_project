# Project Details

## Group Identification

- Group 10
- Eduardo Sampaio, nº66097
- Gonçalo Vicente, nº66118

## Instructions

NOTE - a field is represented by "<[name of field]>", when creating the file, do not type either '<', '>' and the field name, just the value.

1. Compile the project using javac. For example, when on the "src/" directory: "javac -d ./compiled *.java".
2. Create a text file with this structure:


    <number of nodes>
    <node 1 IP address>:<node 1 port>
    <node 2 IP address>:<node 2 port>
    .
    .
    .
    <node n IP address>:<node n port>
    [OPTIONAL] <confusion start> (default 0)
    [OPTIONAL] <confusion duration> (default 0)

An example input file is included in the "test/" directory, with the following contents:

    5
    localhost:8001
    localhost:8002
    localhost:8003
    localhost:8004
    localhost:8005
    1
    2

2. Open a number of different terminals corresponding with the total number of nodes input in the file. 
3. Run the program in each terminal with the following arguments: node ID, input file path, and [OPTIONAL] true (for causing a fork)/false (for correct behaviour). If ommited, the default value for the last field is false. 
Below is an example of how to run the program with 5 nodes using the provided input file, with node 3 causing a fork:
- Node 1: java compiled/Main 1 ../input.txt
- Node 2: java compiled/Main 2 ../input.txt
- Node 3: java compiled/Main 3 ../input.txt true
- Node 4: java compiled/Main 4 ../input.txt
- Node 5: java compiled/Main 5 ../input.txt 

In this example, we are situated in the "src/" directory, where "compiled/" is the directory containing the byte code and "input.txt" is the provided input file in "test/".

Another way of simulating a crash is by running the nodes without passing "true" for a node to cause forking, and just manually killing and rerunning one of the terminals.

## Limitations

- It's not possible to alter the group size while the program is running;
- The number of nodes must be passed as an argument;
- The program does not tolerate byzantine failures;
- The program only tolerates up to n/2 crash faults.

## Print Color Table

The blockchain for each epoch is composed of blocks that belong to a certain epoch. The print may look like the following:

================================================= \
EPOCH - 1 | LEADER - 5 | TIMESTAMP - 20:50:03 \
\>>>> CHAIN >>>> 0 --> 1 \
================================================= \
EPOCH - 2 | LEADER - 4 | TIMESTAMP - 20:50:09 \
\>>>> CHAIN >>>> 0 --> 1 --> 2 \
================================================= \
EPOCH - 3 | LEADER - 5 | TIMESTAMP - 20:50:15 \
\>>>> CHAIN >>>> 0 --> 1 --> 2 --> 3 \
=================================================

Each number represents a block of said epoch. Each epoch number is highlighted by a specific color, with the following meanings:

- red - proposed block
- yellow - notarized block
- green - finalized block