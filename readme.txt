Java8 has been used to build the .java files.
Can be as simple as “javac *.java”.

Run commands take the following format:
Option 1:  
    java CityAndInterstateProcessor <input file> 
Option 2:  
    java CityDegreesProcessor <input file> <root city name>
      (last parameter is optional; defaults to Chicago, 
      the output file doesn’t change due to specs)

For example:
java CityAndInterstateProcessor Sample_Cities.txt 
java CityDegreesProcessor Sample_Cities.txt
java CityDegreesProcessor Sample_Cities.txt Oakland

