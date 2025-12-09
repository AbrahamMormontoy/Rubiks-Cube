javac -d out src/rubikscube/*.java 

java -cp out rubikscube.Solver testcases/scramble09.txt testcases/solution09.txt
