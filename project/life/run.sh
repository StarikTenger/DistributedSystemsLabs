javac --release 8 -Xlint:deprecation -d build -cp "../jar/*" "src/*.java"
# java -classpath "../jar/*;./build/" Main