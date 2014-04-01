find -name "*.java" > sources.txt
mkdir bin
javac -d ./bin -cp .:deps/* @sources.txt
rm sources.txt

