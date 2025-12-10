# Project Details

## Group Identification

- Group 10
- Eduardo Sampaio, nº66097
- Gonçalo Vicente, nº66118

## Instructions

1. Compile the project using javac
2. Create an input file with this structure:


    number of nodes
    node 1 ip:node 1 port
    node 2 ip:node 2 port
    .
    .
    .
    [OPTIONAL] confusion start (default 0)
    [OPTIONAL] confusion duration (default 0)
2. Run the program in "number of nodes" different terminals with the following arguments: node id, file name, [OPTIONAL] crash (boolean - false by default) for each node:
3. Below is an example of how to run the program with 5 nodes using the provided input file, with node 3 crashing:
- Node 1: 1 test/input.txt
- Node 2: 2 test/input.txt
- Node 3: 3 test/input.txt true
- Node 4: 4 test/input.txt
- Node 5: 5 test/input.txt
4. Another way of simulating a crash is by running the nodes without passing true for the crash boolean, and just crashing and rerunning one of the nodes, the input for that would look like this:
- Node 1: 1 test/input.txt 
- Node 2: 2 test/input.txt
- Node 3: 3 test/input.txt
- Node 4: 4 test/input.txt
- Node 5: 5 test/input.txt 

## Limitations

- It's not possible to add or remove nodes while the program is running;
- The number of nodes must be passed as an argument;
- The program does not tolerate byzantine failures;

## Print Color Tabel

- red - proposed block
- yellow - notarized block
- green - finalized block