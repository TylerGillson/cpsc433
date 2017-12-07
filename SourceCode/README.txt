CPSC 433 Semester Project

Daniel Tyler Gillson
Ethan Higgs
David Keizer
Zibin Mei

Our system generates and optimizes solutions for the following problem:

"Finding an optimal assignment of courses and labs to time slots in the week for the University of Calgary's Computer Science Department."

General Usage:

1. In the cpsc433/src directory, compile all java files with: javac *.java
2. Run the system with: java -Xss20m Driver "../config.txt" "../[input file name]"
3. The file config.txt contains the following parameters:

	- Parameters for each of the soft constraint penalties and their respective weights. 

	Genetic Algorithm Parameters:

	1. pop_init	- The number of candidate solutions to create as the initial population.
	2. pop_max 	- The maximum size for any given generation.
	3. cull_num - How many of the least fit members of the population to remove once pop_max is reached.
	4. gen_max	- How many generations to run the genetic algorithm for.

	Output Parameters:

	1. print_data - If set to true, each new generation's average, minimum, and maximum Eval scores are printed to standard output.
	2. print_prs  - If set to true, each new child's Eval score is printed to standard output.
