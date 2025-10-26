# Project Details

## Group Identification

- Group 10
- Eduardo Sampaio, nº66097
- Gonçalo Vicente, nº66118

## Instructions

1. Compile the project using javac
2. Run the program in five different terminals with the following arguments (numberOfNodes nodeId serverPort ipAddresses clientPorts) for each node:
  - Node 1: 5 1 8001 localhost localhost localhost localhost 8002 8003 8004 8005
  - Node 2: 5 2 8002 localhost localhost localhost localhost 8001 8003 8004 8005
  - Node 3: 5 3 8003 localhost localhost localhost localhost 8001 8002 8004 8005
  - Node 4: 5 4 8004 localhost localhost localhost localhost 8001 8002 8003 8005
  - Node 5: 5 5 8005 localhost localhost localhost localhost 8001 8002 8003 8004 

## Limitations

- It's not possible to add or remove nodes while the program is running;
- The number of nodes must be passed as an argument;
