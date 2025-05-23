# My notes
# Java Fundamentals
**Java Architecture**
Source Code -> Compiler -> Java Byte Code -> JVM (=Java Virtual Machine) -> any platform (Mac, PC, UNIX, Android, etc.)
* Java: Fast and portable (has best of compiler and interpreter)

**Java Files**
- MyClass.java: source file -> there is normally one Java class per .java file
- MyClass.class = executable file (by JVM)
- Main method: 'public static void main(String [] args)' OR 'public static void main(String...args)'

**Creating Java Classes**
right click on the file -> select 'New' -> Java class
- Main method
public class JavaClass {
   public static void main(String [] args) {
      //code
   }
}

**Javadoc** - documentation tool generated from the code
- Javadoc comment: /** comment */ -> helps understanding of details of parameter or method

**Primitive Data Types** - byte, short, int (32bit), long (64bit), float, double (twice bigger than 'float'), char (16bit), boolean
- long: 10L ->'L' is Long integer literal that should be interpreted as a long type, not default integer type
- float: 5f OR 5F -> 'f' is float literal
- char: can use Unicode(='\u' + '4 digit number')
* Except these 8 types, every other data types are class
* All data types in java are signed

**Format output** - when using 'printf'
- %d: string format
- %.2f: float format
- %c: char format (print single character)
- %b: boolean format

**Strings** - objects in java
- String declaration and assignment
ex: String s1 = "Hello"; OR String s2 = new String("Hello"); 
* 's1' is reference for the String object
* 's2' is reference for new String object, not same as 's1'
- String concatenation: Use '+' for small number of Strings. 
* For large number of Strings, use 
StringBuilder builder = new StringBuilder();
builder.append("string")
builder.append("string1")
String str = builder.toString();
- String Formatting: String.format("%s %s", s1, s2)
*Strings are immutable

**String methods**
- int length()
- char charAt(index)
- String trim(): chop the string and get the new string
- boolean startsWith(String)
- int indexOf(int): find index of character
- int indexOf(String): if there is substring, find index of it
- String substring(int)
- String substring(int, int)

**Special Characters**
- \n (newline)
- \t (tab)
- \" (double quote)
- \' (single quote)
- \\ (backslash)
- \b (backspace): chop off the last character
- \uXXXX (inser the Unicode)
- \r, \f: these two are obsolete now

**Arrays** - objects
- Syntax
ex: int[] intArray; //declare arrays
intArray = new int[5]; //actually create object
intArray[0] = 500; //initialize arrays

int [] intArray1 = {2, 7, 37, 35}; //declare, create, initialize at the same time

- Iterate an array:
  ex1:
  for(int i = 0; i < intArray1.length; i++) {
  if(i > 0) {
  System.out.print(", ");
  }
  System.out.print(intArray1[i]);
  }
  ex2:
  for(int value : intArray1) {
  System.out.print(value);
  System.out.print(", ");
  }
  System.out.print("\b\b"); //clean up the last ','

- Arrays of Arrays:
  ex: char[][] arrayName = new char[3][];
  arrayName[0] = new char[5]; //array of 5 in the index 0 in the array of 3
* Doesn't need to be same row and column

**Command-Line Arguments**
public static void main(String [] args) {
   for(int i = 0; i < args.length; i++) {
      String message = String.format("Argument %d is %s", i, args[i]);
      System.out.println(message);
   }
}

- From the command line: java 'name of the program' 'argument1' 'argument' "String argument"

- How to run: run once -> Go to 'Run Configurrations' -> type command-line arguments in 'Build and Run'

**Packages**
- provide a way to organize classes into logical groups
- can have sub-packages (separated by .(dots))
- The package name becomes part of the class name
ex: java.util.Date , java.sql.Date

**Import**
- provide a shorthand for the fully-qualified package name
- do not increase the size of the compiled .class files
- using this appears at the top of the file
- Do not need an import in the following cases: (1) java.lang package, (2) class you would import is in in the package as the class that needs to use it

**CLASSPATH** - an environment varaible that contians a list of directories that contain .class files, package base directories, or other resources your application needs to access
- .(current directory): implicitly on the CLASSPATH if I don't set it
- can use -classpath command line param
- IDEs manage this for me

# Object-Oriented Programming
Overall structure in the class: package name, import, class, getter & setter.

**Object References**
- Working with object means using object references 
- Refer to objects
*Heap is where memory is allocated for the object
ex: Date dt; //create reference
dt = new Date(); //create object
- Reference Equality: check if(p1 = p2) //compares the reference
- Object Equality: check if(p1.equals(p2)) //equals() compares variables
- instanceOf(): checks which object it is using

**Static Variables and Methods**
- Instance Variables
1) Each object (instance) gets its own copy of all of the instance variables defined in its class
2) Most variables should be instance variables
ex: Allows two date objects to represent different dates
- Static Variables
1) it is associated with the class not with instances
2) Use in special cases where you won't create instances of a class, or all instances should share the same values 
ex: If the variables of a Date class where static, all dates in a program would represent the same date

- Instance methods
1) it is associated with a specific instance (object)
2) Invoked from a reference that refers to an instance

- Static methods
1) it is associated with a class
2) Invoked by using the class name (Can be invoked from a reference)
3) cannot access instance variables
ex:
public class StaticExample {
   private int myInstanceVariable;
   public static void main(String [] args) {
      StaticExample instance = new StaticExample();
      instance.myInstanceVariable = 10;
      instance.myInstanceMethod(); 
   }
   public void myInstanceMethod() { }
}

**Getter and Setter (Accessor and Mutator)**
- Methods for getting and setting instance variables
- Make variables private and only allow access through getters and setters
- Not required to provide getter and setter for all variables
- IDE can generate them: go to 'Code' -> 'Generate' -> select 'Getter and Setter' -> select specific variables -> OK

**Constructor Methods**
- Must match the class name
- Like a method with a return type
- All classes have at least one --> default constructor written by the compiler if you don't write any
- can have multiple constructors (with different parameter types)
- Constructors invoke each other with 'this(...)'
- Constructors invoke parent constructor with 'super(...)'
*this(...), super(...): always first statement

**Inheritance**
- Create hierarchy (Parent and Child class)
- Use the 'extends' keyword
ex:
public class Employee extends Person { }
- Inherited all instance variables (even private ones) and non-private, non-static methods
- Not inherited constructors, static methods, private methods

**Method Overriding**
- A subclass replaces an inherited method by redefining it
1) Argument list must be the same
2) Return type must be the same
3) Must not make access modifier more restrictive (ex: cannot make public to private)
4) Must not throw new or broader checked exceptions
- Can call the overridden version of the method by using 'super' (and then add additional codes)
- Use '@Override' to redefine

**Implementing a hashCode() Method** - One of the common overriding methods (toString(), equals(), hashCode())
*Hashtable was the data structure to put objects and get it by index int
1) Whenever it is invoked on the same object more than once, the hashCode method must return the same integer
2) If two objects are equal according to the equals(Object) method, then it must produce the same integer result
3) Producing distinct integer results for unequal objects may improve the performance of hash tables

How to implement
1) Hash each value in the object that you want included in the hash
- For integer numbers, use the number as the hash for that value
- For Strings, call the object's hashCode() method
- For Objects that may be null, can use the Objects.hashCode() method
- For arrays, either hash each element in the array, or call Arrays.hashCode()
2) While computing a hash, we usually multiply by 31
ex:
public int hashCode() { 
    int hash = 7;
    hash = 31 * hash + (int) id;
    hash = 31 * hash + (name == null ? 0: name.hashCode());
    hash = 31 * hash + (email == null ? 0 : email.hashCode());
    return hash;
}

**Method Overloading**
- Reuse a method name with a different argument list
ex:
public void print(boolean)
public void print(char) ...etc

**'Final' keyword**
- Final Variables: public final int myVariable = 10; --> Can't be changed after a value is assigned
- Final Reference Variables: public final ArrayList list = new ArrayList(); --> can't assign different reference
- Final Methods: public final void myMethod() { } --> can't override

**'this' reference** - Reference that all objects have
- When to use it: using instance variable when local variable's name is same as it
*usually used in setter

**Enums** - like a java class to write codes
- Use where you would otherwise have an unrestricted String with only a few values being valid
ex:
public enum Gender {
    Male, Female; //only few valid variables
    @Override
    public String toString() {
        return this == Male ? "m" : "f";
    }
--> can be set only to Gender.Male or Gender.Female

**Object-Oriented Design Overview**
- Decompose a program into classes
- Use separate classes to represent each concept
- Identify relationships between classes (Is-A relationships with inheritance OR Has-A and Uses-A relationships with references)
- Not all fields need individual getters and setters
- Break up classes that have too many responsibilities
- Naming: reflect their responsibilities (classes -> Noun, methods -> verb)
ex:
public class MyClass {
// Static Variables
// Instance Variables
// Main Method (if it exists)
// Constructors
// Methods (grouped by functionality, not by accessibility, not by static vs instance, etc)
}

**Java Records**
- Have jave classes that only exist to represent data (sometimes referred to as POJOs)
- Java POJO -> IDE can generate it with only instance variables
ex: public record Pet(int id, String name, String type) {}: same as Pet class (including getters, overrided methods like equals())
- Features:
1) All fields are final ->Records are immutable (We can add methods to reassign the variable by creating new one)
2) Simplified constructor syntax
3) Automatic getters: access by using field name -> p.name();
4) Automatic equals, hashcode, toString

**Exceptions and Exception Handling** - won't compile if exception isn't handled
- Exception occurs by bugs from the code or internet problem
- Partial Exception Class Hierarchy: Throwable -> Exception, Error
- Exception Handling:
1) 'try/catch' (and 'finally'):
- can have multiple 'catch' blocks
- can put different exceptions with '||' with same handling
- put more specific exceptions first in multiple 'catch' blocks
ex: normal 'try/catch/finally'
try {
    Scanner scan = new Scanner(file);
}catch (FileNotFoundException ex) {
    ex.printStackTrace(); //<- print the error
    //or swallow the exception
}finally { //execute no matter what happens (in c++, it is like a destructor)
    if(scan != null) {
        scan.close(); //we have to close the file not to face another error
    }
}
ex: 'try' with resources
try(Scanner scan = new Scanner(file)) { //java automatically close the file -> no need for 'finally'

}catch (FileNotFoundException ex) { }
2) add 'throws FileNotFoundException' next to the method()
ex: 
private static void processFile(File file) throws FileNotFoundException { }
//in main method
processFile(file); --> has error, do Try/Catch (better option) or add 'throws FileNotFoundException'
3) create my own exception class for specific action in my code
ex: create new class -> put 'extends Exception'
public class SomethingBadHappenException extends Exceptions { }
*you can have Super Class Constructors (go to Code -> Generate)

**Exceptions: Checked vs. Unchecked Exceptions**
- Checked Exceptions: like IOException, something that has to be handled (For Unchecked, normally don't handle)
- Unchecked Exceptions: NullPointerException, IndexOutOfBoundsException -> fix the code instead of exception handling

**Polymorphism** - means "many forms"
- Objects can take on many forms in object-oriented program: has inheritance hierarchy
- The form is represented by the type of the reference that refers to the object
ex:
Employee emp = new Employee();
Person emp = new Employee();
Object emp = new Employee();
- Result in Memory
Person emp = new Employee(); -> cannot access 'hireDate' and 'salary' from Employee part from the 'emp' reference
- Reasons for Polymorphism : To Get the same result
1) can create collections of a parent type that contain children of different types
2) Parameters in a method call that expect a parent reference or object but receive a child of the expected type

**Abstract Classes**
- Abstract Method (which requires an Abstract Class): write common code to inherit for specific method
ex: 
public abstract class Vehicle {
    public abstract void go(); //must be overridden in child classes unless the child is abstract
}
- Polymorphic Method Invocation
ex:
Vehicle v = new Car(); //cannot do 'Vehicle v = new Vehicle();' -> illegal because it's abstract class
v.go();
- features:
1) cannot be instantiated
2) can be used as reference types (polymorphism)
3) can be used as array types (polymorphism)
4) may have non-abstract methods
5) don't have to have abstract methods
6) provide a guarantee: to invoke a real (non-abstract) method

**Interfaces**
- Features:
1) cannot be instantiated
2) can be used as reference types
3) can be used as collection (array) types
4) may not have non-abstract methods (All methods are abstract)
5) All methods are public (some exceptions in recent java)
6) provide same guarantee like abstract classes
7) can implement any number of interfaces and still subclass some other class
8) breaks inheritance barrier of polymorphism
9) can have constant variables (public, static, final)
10) can have instance methods with bodies (must be declared as 'default') (In Java 8 and later)
ex: default void myDefaultMethod() { }
11) can have static methods (with bodies) (In Java 8 and later)
12) can have private methods (useful as helper methods to default methods) (In Java 9 and later)

Interface ex:
Moveable m = new Car();
Moveable m = new Person();
Moveable m = new Dog();
Moveable m = new Moveable(), new Vehicle(); -> illegal

- How to create an Interface: no single inheritance limit with interfaces
implementing classes must implement all methods of the interface and each parent interface or be declared abstract
ex:
public interface MyInterface extends, Moveable, Comparable {
    void myMethod();
    void myOtherMethod();
}
- How to implement an Interface
ex:
public class Person implements Moveable {
    public void go() { }
}
public abstract class Vehicle implements Moveable { }
public class Employee extends Person implements Moveable, Comparable {
 //must write all methods from both interfaces 
}

**Java Collections**
- primitive types cannot be stored in collections
- These are interface: Collection (<-List, Set(<-SortedSet <-NavigableSet), Queue(<-Deque)), Map(<-SortedMap <-NavigableMap), Iterator(<-ListIterator)
1) List (interface)
- a sequence of elements accessed by index: get(), set()
- implementations: ArrayList, LinkedList
- List support a more powerful iterator, ListIterator
2) Set (interface)
- a collection that contains no duplicates: add(), contains(), remove()
- implementations: HashSet, TreeSet, LinkedHashSet
3) Queue (interface)
- a collection designed for holding elements prior to processing: add(), peek(), remove()
- implementations: ArrayDeque, LinkedList, PriorityQueue
4) Deque (interface)
- a queue that supports efficient insertion and removal at both ends: addFirst(), addLast(), etc.
- implementations: ArrayDeque, LinkedList
* Java doesn't have Stack -> use A Deque instead (push() ->addFirst(), etc.)
5) Map (interface)
- a collection that maps keys to values (keys are unique): put(k,v), get(), contains(), remove(), keySet(), values(), entrySet()
- implementations: HashMap, TreeMap, LinkedHashMap

**Using Collections**
- can iterate over collections (can throw exception for modifying while iterating)
- Equality Checking: override equals() if want equality checks to be based on value rather than identity
- Hashing-Based Collections: override hashCode() if using collections with hash tables
Rules: if equals() is based on identity, so should hashCode() be.

- Sorted Collections: TreeSet (BST), TreeMap (BST), PriorityQueue (binary heap)
1) The elements of a sorted collection must be sortable and must be able to compare
2) should implement 'Comparable' interface or Comparator for tree-based collections
ex: public class TimeOfDay implements Comparable<TimeOfDay> {
//Constructor, getters, setters, overrided CompareTo()
}
3) can't change information in objects that are used as keys in data structures

**Copying Objects** - copy the chessboard to check certain move if that makes "check" in chess
- It is common to need to make a copy of an object
- Ways to copy an object
1) Shallow copy: copy the variable values from the original object to the copy (primitive and object references)
-> usually not what you want: changing can be applied to original
2) Deep copy: copy the object and all objects it references, recursively
*immutable objects do not need to be copied and can be safely shared
ex: Strings, Integer, Boolean, Double, etc. (the object versions of the primitives), etc.

- Writing classes that support copying
1) shallow copy
ex: 
public class Person implements Cloneable { //Cloneable is marker interface
    @Override
    public Person clone() { //override clone() method (return type should be compatible with Object, can be subclass)
        try {
            return (Person) super.clone();
        }catch (CloneNotSupportedException e) {
            throw new RuntimeException(e); 
        }
   }
}
2) deep copy
ex: 
public class Person2 implements Cloneable {
    @Override
    public Person2 clone() {
        try {
            Person2 clone = (Person2) super.clone();
            Date cloneBirthDate = (Date) getBirthdate().clone();
            clone.setBirthdate(cloneBirthdate);
            return clone;
        }catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
*shallow copy cannot be applied to List -> could make changes in List
ex: deep copy
   public Person2 clone() {
       try {
           Team clone = (Team) super.clone();
           List<Person> cloneMembers = new ArrayList<>();
           for(Person person : members) {
               Person personClone = person.clone();
               cloneMembers.add(personClone);
           }
           clone.members = cloneMembers;
           return clone;
       }catch (CloneNotSupportedException e) {
           throw new RuntimeException(e);
       }
   }

**Inner Classes** - declaring classes in the class
- example of use: each class should have different implementations for iterator

- Types of inner classes
1) static inner class
- can be defined inside of the class
- cannot access variables directly from outer classes
  *should declare it as close as to where we use
2) inner classes without ‘static’
- can access variables from containing class
3) local inner class: declare it inside of the method
- allows us to put close to the location where we use
- no need to create the constructor
- can see some of local variables
- restriction: local variables only exist in the block of method -> local inner classes are not impacted outside of the method -> can only access either final local variable or effectively final variable
  ex: public Iterator(final int increment) { }
4) Anonymous inner class: declare it inside of expression
- has benefits from local inner class
- No declared name for the inner class is needed
- common in Java (in the past, it was used for event handler)
  ex:
  return new Iterator() {
  //variables, overrided methods
  };

**Design Principles**
- Principles
1) Design is inherently iterative: Design -> implement -> test -> design -> ...
*Do not design everything before beginning implementation -> do it in short iterations
2) Abstraction: create higher-level, domain-specific classes (represents core concepts)
*In object-oriented programming, abstractions are represented by classes
- each class has a carefully designed public interface that defines how the rest of the system interacts with it
- a client can invoke operations on an object without understanding
3) Good naming: a central part of abstraction is giving names
- should represent their function or purpose
- class and variable names are usually nouns, methods are usually verbs
4) Single-responsibility
- each class and method should hae a single responsibility
- each class should represent one, well-defined concept
- each method should perform one, well-defined task
- methods that need to perform multiple tasks -> create sub-methods for each task
- cohesive classes and methods are easy to name
5) Decomposition: large problems subdivided into smaller sub-problems
- solutions to sub-problems are recombined into solutions to larger problems
- strongly related to Abstraction
- the solution to each sub-problem is encapsulated in its own abstraction (class or function)
- Levels of decomposition: System - Subsystem - Packages - Classes - Methods
6) Good Algorithm & Data Structure selection
7) Low Coupling: minimize the number of other classes a class interacts with or knows about
- it reduces ripple effects when a class changes
- a class should hide or "encapsulate" its internal implementation that are not essential
- all internal implementation should be "private" unless there's a good reason for "public" and "protected"
- a class public interface should be as simple and minimal
8) Avoid Code duplication
- bugs can be duplicated
- makes program longer

**Streams & Files**
- Ways to Read/Write Files
1) Streams: read or write a file sequentially
- we can use Binary-Formatted (bytes) or Text-Formatted Data (characters)
- InputStream and OutputStream: reading/writing bytes
  (1) Input Streams : there is 'Filter Input Streams' to decompress, decrypt data, etc.
  (2) Output Streams : there is 'Filter Output Streams' to compress, encrypt data, etc.
- Reader and Writer: reading/writing characters
ex: FileReader, FileWriter object & PrintWriter
    a. InputStreamReader: convert from an input stream to a reader
    b. OutputStreamWriter: convert from an output stream to a writer
2) Scanner Class: Tokenize stream input (new InputStreamReader(new FileInputStream("myfile.txt"));)
3) Files Class: Read, copy etc. whole files (new OutputStreamWriter(new FileOutputStream("myfile.txt"));)
- Used to represent, create, or delete a file, but not to read one
ex:
File file = new File("/user/MyFile.txt");
if(file. exists()) { } //check file existence
file.createNewFile(); //create a file
file.delete() //delete a file
List<String> fileContents = Files.readAllLines(path) //read whole file into list
4) RandomAccessFile Class: Use a file pointer to read/write from to any location in a file (not used very often)

