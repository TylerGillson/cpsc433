CPSC 433 Semester Project

Daniel Tyler Gillson
Ethan Higgs
David Keizer
Zibin Mei

Our system generates and optimizes solutions for the following problem:

"Finding an optimal assignment of courses and labs to time slots in the week for the University of Calgary's Computer Science Department."

If the provided input file contains a solvable problem instance, our system will generate an initial population via an Or-tree-based search.
It will then evolve the population using roulette wheel selection and a cross-over/mutation function.
The cross-over/mutation function is implemented via another Or-tree-based search using an alternate search control.

If the provided input file contains an unsolvable problem instance, our system will exit and display an error message.

General Usage:

1. In the cpsc433/src directory, compile all java files with: javac *.java
2. Run the system with: java -Xss20m Driver "../config.txt" "../[input file name]"
3. The file config.txt contains the following system parameters:

	Soft Constraint Parameters:
	
	1. minfilledWeight  - The weight value for the courseminPenalty and labminPenalty parameters.
	2. prefWeight 	    - The weight value for the preference penalties contained in the input file.
	3. pairWeight 	    - The weight value for the notpairedPenalty parameter.
	4. secdiffWeight    - The weight value for the sectionPenalty parameter.
	5. courseminPenalty	- Penalty to incur if a course time slot is not assigned the minimum number of courses indicated in the input file.
	6. labminPenalty	- Penalty to incur if a lab time slot is not assigned the minimum number of labs indicated in the input file.
	7. notpairedPenalty	- Penalty to incur if a pair of courses/labs indicated in the input file are not assigned the same time slot.
	8. sectionPenalty	- Penalty to incur if a pair of sections for a course are assigned the same (or overlapping) time slot(s).

	Genetic Algorithm Parameters:

	1. pop_init	- The number of candidate solutions to create as the initial population.
	2. pop_max 	- The maximum size for any given generation.
	3. cull_num - How many of the least fit members of the population to remove once pop_max is reached.
	4. gen_max	- How many generations to run the genetic algorithm for.

	Output Parameters:

	1. print_data - If set to true, each new generation's average, minimum, and maximum Eval scores are printed to standard output.
	2. print_prs  - If set to true, each new child's Eval score is printed to standard output.
