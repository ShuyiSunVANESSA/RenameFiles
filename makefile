# super simple makefile
# call it using 'make NAME=name_of_code_file_without_extension'
# (assumes a .java extension)
NAME = "rename"

default:
	@echo "Compiling..."
	javac $(NAME).java

run: 
	@echo "Running..."
	java $(NAME)

clean:
	rm -rf *.class
