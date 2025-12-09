javac -d out src/rubikscube/*.java 

java -cp out rubikscube.Solver testcases/scramble15.txt testcases/solution15.txt
