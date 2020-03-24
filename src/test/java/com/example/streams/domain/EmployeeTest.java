package com.example.streams.domain;

import com.example.streams.repository.EmployeeRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * +Overview+
 * <p>
 * The addition of the Stream was one of the major features added to Java 8. This in-depth tutorial is an introduction to the many functionalities supported by streams, with a focus on simple, practical examples.
 * To understand this material, you need to have a basic, working knowledge of Java 8 (lambda expressions, Optional, method references).
 * <p>
 * +Introduction+
 * <p>
 * First of all, Java 8 Streams should not be confused with Java I/O streams (ex: FileInputStream etc); these have very little to do with each other.
 * Simply put, streams are wrappers around a data source, allowing us to operate with that data source and making bulk processing convenient and fast.
 * A stream does not store data and, in that sense, is not a data structure. It also never modifies the underlying data source.
 * This functionality – java.util.stream – supports functional-style operations on streams of elements, such as map-reduce transformations on collections.
 * Let’s now dive into few simple examples of stream creation and usage – before getting into terminology and core concepts.
 * <p>
 * Reference: https://stackify.com/streams-guide-java-8/
 */

public class EmployeeTest {
    private EmployeeRepository employeeRepository;

    private Employee[] empArray;

    private List<Employee> empList;

    @Before
    public void setUp() {
        this.empArray = new Employee[]{
                new Employee(1L, "Jeff Bezos", 100_000.0),
                new Employee(2L, "Bill Gates", 200_000.0),
                new Employee(3L, "Mark Zuckerberg", 300_000.0)
        };
        this.empList = Arrays.asList(this.empArray);
    }

    @Test
    public void mustResolveStreamCreation() {
        //act
        // Let’s first obtain a stream from an existing array:
        Stream<Employee> employeeStream1 = Stream.of(this.empArray);
        // We can also obtain a stream from an existing list:
        Stream<Employee> employeeStream2 = this.empList.stream();
        // And we can create a stream from individual objects using Stream.of():
        Stream<Employee> employeeStream3 = Stream.of(this.empArray[0], this.empArray[1], this.empArray[2]);
        // Or simply using Stream.builder():
        Stream.Builder<Employee> empStreamBuilder = Stream.builder();

        empStreamBuilder.accept(this.empArray[0]);
        empStreamBuilder.accept(this.empArray[1]);
        empStreamBuilder.accept(this.empArray[2]);

        Stream<Employee> employeeStream4 = empStreamBuilder.build();

        // assert
        assertNotNull(employeeStream1);
        assertNotNull(employeeStream2);
        assertNotNull(employeeStream3);
        assertNotNull(employeeStream4);
    }

    // Stream Operations
    // Let’s now see some common usages and operations we can perform on and with the help of the stream support in the language.

    /**
     * forEach() is simplest and most common operation; it loops over the stream elements, calling the supplied function on each element.
     * <p>
     * This will effectively call the salaryIncrement() on each element in the empList.
     * <p>
     * forEach() is a terminal operation, which means that, after the operation is performed, the stream pipeline is considered consumed, and can no longer be used. We’ll talk more about terminal operations in the next section.
     */
    @Test
    public void whenIncrementSalaryForEachEmployee_thenApplyNewSalary() {
        // act
        // this.empList.stream().forEach(e -> e.salaryIncrement(10.0));
        // The forEach is so common that is has been introduced directly in List, Iterable, Map etc:
        this.empList.forEach(e -> e.salaryIncrement(10.0));

        // assert
        assertThat(this.empList, contains(
                hasProperty("salary", equalTo(110_000.0)),
                hasProperty("salary", equalTo(220_000.0)),
                hasProperty("salary", equalTo(330_000.0))
        ));
    }

    /**
     * map() produces a new stream after applying a function to each element of the original stream. The new stream could be of different type.
     * <p>
     * Here, we obtain an Integer stream of employee ids from an array. Each Integer is passed to the function employeeRepository::findById() – which returns the corresponding Employee object; this effectively forms an Employee stream.
     */
    @Test
    public void whenMapIdToEmployees_thenGetEmployeeStream() {
        // arrange
        this.employeeRepository = new EmployeeRepository();
        Long[] empIds = {1L, 3L};

        // act
        List<Employee> result = Stream.of(empIds)
                .map(this.employeeRepository::findById)
                .collect(Collectors.toList());

        // assert
        assertThat(result, contains(
                hasProperty("fullName", equalTo("Jeff Bezos")),
                hasProperty("fullName", equalTo("Mark Zuckerberg"))
        ));
    }

    /**
     * collect
     * We saw how collect() works in the previous example; its one of the common ways to get stuff out of the stream once we are done with all the processing.
     * <p>
     * collect() performs mutable fold operations (repackaging elements to some data structures and applying some additional logic, concatenating them, etc.) on data elements held in the Stream instance.
     * <p>
     * The strategy for this operation is provided via the Collector interface implementation. In the example above, we used the toList collector to collect all Stream elements into a List instance.
     */
    @Test
    public void whenCollectStreamToList_thenGetList() {
        List<Employee> employees = this.empList.stream().collect(Collectors.toList());

        assertEquals(this.empList, employees);
    }

    /**
     * filter
     * Next, let’s have a look at filter(); this produces a new stream that contains elements of the original stream that pass a given test (specified by a Predicate).
     * <p>
     * In the example below, we first filter out null references for invalid employee ids and then again apply a filter to only keep employees with salaries over a certain threshold.
     * <p>
     * Let’s have a look at how that works:
     */
    @Test
    public void whenFilterEmployees_thenGetFilteredStream() {
        // arrange
        this.employeeRepository = new EmployeeRepository();
        Long[] empIds = {1L, 2L, 3L, 4L};

        // act
        List<Employee> result = Stream.of(empIds)
                .map(this.employeeRepository::findById)
                .filter(employee -> employee != null) // Better to create a predicate that evaluate employee object
                .filter(employee -> employee.getSalary() >= 200_000.0)
                .collect(Collectors.toList());

        // assert
        assertThat(result, contains(
                hasProperty("fullName", equalTo("Bill Gates")),
                hasProperty("fullName", equalTo("Mark Zuckerberg"))
        ));
    }

    /**
     * findFirst
     * findFirst() returns an Optional for the first entry in the stream; the Optional can, of course, be empty.
     * <p>
     * Here, the first employee with the salary greater than 100_000 is returned. If no such employee exists, then null is returned.
     */
    @Test
    public void whenFindFirst_thenGetFirstEmployeeInStream() {
        // arrange
        this.employeeRepository = new EmployeeRepository();

        // act
        Employee result = this.empList
                .stream()
                .map(Employee::getId)
                .map(this.employeeRepository::findById)
                .filter(employee -> employee.getSalary() > 100_000)
                .findFirst()
                .orElse(null);

        // assert
        assertNotNull(result);
        assertEquals("Bill Gates", result.getFullName());
    }

    // Lazy Evaluation
    // One of the most important characteristics of streams is that they allow for significant optimizations through lazy evaluations.
    // Computation on the source data is only performed when the terminal operation is initiated, and source elements are consumed only as needed.
    // All intermediate operations are lazy, so they’re not executed until a result of a processing is actually needed.
    // For example, consider the findFirst() example above {@link whenFindFirst_thenGetFirstEmployeeInStream}. How many times is the map() operation performed here? 4 times, since the input array contains 4 elements?
    // Stream performs the map and two filter operations, one element at a time.
    // It first performs all the operations on id 1. Since the salary of id 1 is not greater than 100000, the processing moves on to the next element.
    // Id 2 satisfies both of the filter predicates and hence the stream evaluates the terminal operation findFirst() and returns the result.
    // No operations are performed on id 3 and 4.
    // Processing streams lazily allows avoiding examining all the data when that’s not necessary. This behavior becomes even more important when the input stream is infinite and not just very large.

    /**
     * toArray
     * We saw how we used collect() to get data out of the stream. If we need to get an array out of the stream, we can simply use toArray().
     * <p>
     * The syntax Employee[]::new creates an empty array of Employee – which is then filled with elements from the stream.
     */
    @Test
    public void whenStreamToArray_thenGetArray() {
        // arrange
        Employee[] employees = this.empList.stream().toArray(Employee[]::new);

        // assert
        assertThat(this.empList.toArray(), equalTo(employees));
    }

    /**
     * flatMap
     * A stream can hold complex data structures like Stream<List<String>>. In cases like this, flatMap() helps us to flatten the data structure to simplify further operations.
     * <p>
     * Notice how we were able to convert the Stream<List<String>> to a simpler Stream<String> – using the flatMap() API.
     */
    @Test
    public void whenFlatMapEmployeeNames_thenGetNameStream() {
        // arrange
        List<List<String>> nameNested = Arrays.asList(
                Arrays.asList("Jeff", "Bezos"),
                Arrays.asList("Bill", "Gates"),
                Arrays.asList("Mark", "Zuckerberg")
        );

        // act
        List<String> names = nameNested.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        // assert
        assertThat(names, contains(
                equalTo("Jeff"),
                equalTo("Bezos"),
                equalTo("Bill"),
                equalTo("Gates"),
                equalTo("Mark"),
                equalTo("Zuckerberg")
        ));
    }

    /**
     * peek
     * We saw forEach() earlier in this section, which is a terminal operation. However, sometimes we need to perform multiple operations on each element of the stream before any terminal operation is applied.
     * <p>
     * peek() can be useful in situations like this. Simply put, it performs the specified operation on each element of the stream and returns a new stream which can be used further. peek() is an intermediate operation.
     * <p>
     * Here, the first peek() is used to increment the salary of each employee. The second peek() is used to print the employees. Finally, collect() is used as the terminal operation.
     * <p>
     * Note: Runs this alone as the empList is static.
     */
    @Test
    public void whenIncrementSalaryUsingPeek_thenApplyNewSalary() {
        // act
        List<Employee> result = this.empList.stream()
                .peek(employee -> employee.salaryIncrement(15.0))
                .peek(EmployeeTest::printNewSalary)
                .collect(Collectors.toList());

        // assert
        assertThat(result, contains(
                hasProperty("salary", equalTo(115_000.0)),
                hasProperty("salary", equalTo(230_000.0)),
                hasProperty("salary", equalTo(345_000.0))
        ));
    }

    private static void printNewSalary(Employee employee) {
        System.out.println(employee.getFullName() + "' new salary: " + employee.getSalary());
    }

    // Method Types and Pipelines
    // As we’ve been discussing, stream operations are divided into intermediate and terminal operations.
    // Intermediate operations such as filter() return a new stream on which further processing can be done. Terminal operations, such as forEach(), mark the stream as consumed, after which point it can no longer be used further.
    // A stream pipeline consists of a stream source, followed by zero or more intermediate operations, and a terminal operation.

    /**
     * Here’s a sample stream pipeline, where empList is the source, filter() is the intermediate operation and count is the terminal operation.
     */
    @Test
    public void whenStreamCount_thenGetElementCount() {
        // act
        long empCount = this.empList.stream()
                .filter(employee -> employee.getSalary() > 200_000)
                .count();

        // assert
        assertEquals(1L, empCount);
    }


    /**
     * Some operations are deemed short-circuiting operations. Short-circuiting operations allow computations on infinite streams to complete in finite time.
     * <p>
     * Here, we use short-circuiting operations skip() to skip first 2 elements, and limit() to limit to 2 elements from the infinite stream generated using iterate().
     */
    @Test
    public void whenLimitInfiniteStream_thenGetFiniteElements() {
        // arrange
        Stream<Integer> infiniteStream = Stream.iterate(3, i -> i * 3); // result {3, 9, 27, 81, 243, 729, 2187}

        // act
        List<Integer> result = infiniteStream
                .skip(2) // Will skip the first elements until get the n(2) -> 3, 9.
                .limit(4) // Will take into consideration the next n(4) (skipping 3 and 9) -> 27, 81, 243, 729.
                .collect(Collectors.toList());

        // assert
        assertEquals(Arrays.asList(27, 81, 243, 729), result);
    }

    //Comparison Based Stream Operations

    /**
     * Let’s start with the sorted() operation – this sorts the stream elements based on the comparator passed we pass into it.
     * For example, we can sort Employees based on their names:
     */
    @Test
    public void whenSortStream_thenGetSortedStream() {
        // act
        List<Employee> result = empList.stream()
                //.sorted((e1, e2) -> e1.getFullName().compareTo(e2.getFullName())) // This is also a valid way
                .sorted(Comparator.comparing(Employee::getFullName))
                .collect(Collectors.toList());

        // Note that short-circuiting will not be applied for sorted().
        // This means, in the example above, even if we had used findFirst() after the sorted(), the sorting of all the elements is done before applying the findFirst(). This happens because the operation cannot know what the first element is until the entire stream is sorted.

        // assert
        assertEquals("Bill Gates", result.get(0).getFullName());
        assertEquals("Jeff Bezos", result.get(1).getFullName());
        assertEquals("Mark Zuckerberg", result.get(2).getFullName());
    }

    /**
     * As the name suggests, min() and max() return the minimum and maximum element in the stream respectively, based on a comparator. They return an Optional since a result may or may not exist (due to, say, filtering).
     */
    @Test
    public void whenFindMin_thenGetMinElementFromStream_Optional() {
        // arrange
        Stream<Integer> numbers = Stream.iterate(2, i -> i * 2)
                .limit(5);

        // act
        Optional<Integer> min = numbers.min(Comparator.comparingInt(Integer::intValue));

        // assert
        assertTrue(min.isPresent());
        assertEquals(new Integer(2), min.get());

    }

    @Test
    public void whenFindMin_thenGetMinElementFromStream() {
        // act
        Employee firstEmp = empList.stream()
                .min((e1, e2) -> e1.getId().intValue() - e2.getId().intValue())
                .orElseThrow(NoSuchElementException::new);

        // assert
        assertEquals(new Long(1), firstEmp.getId());
    }

    /**
     * We can also avoid defining the comparison logic by using Comparator.comparing():
     */

    @Test
    public void whenFindMax_thenGetMaxElementFromStream() {
        // act
        Employee maxSalEmp = empList.stream()
                .max(Comparator.comparing(Employee::getSalary))
                .orElseThrow(NoSuchElementException::new);

        // assert
        assertEquals(300_000.0, maxSalEmp.getSalary(), 0.0);
    }

    /**
     * distinct() does not take any argument and returns the distinct elements in the stream, eliminating duplicates. It uses the equals() method of the elements to decide whether two elements are equal or not.
     */

    @Test
    public void whenApplyDistinct_thenRemoveDuplicatesFromStream() {
        // arrange
        List<Integer> intList = Arrays.asList(2, 3, 7, 11, 2, 3, 3, 11, 13);

        // act
        List<Integer> result = intList.stream()
                .distinct()
                .collect(Collectors.toList());

        // assert
        assertEquals(Arrays.asList(2, 3, 7, 11, 13), result);

    }

    /**
     * allMatch, anyMatch, and noneMatch
     * These operations all take a predicate and return a boolean. Short-circuiting is applied and processing is stopped as soon as the answer is determined.
     */
    @Test
    public void whenApplyMatch_thenReturnBoolean() {
        // arrange
        List<Integer> intList = Arrays.asList(2, 4, 5, 6, 8);

        // act
        boolean allEven = intList.stream().allMatch(i -> i % 2 == 0);
        // allMatch() checks if the predicate is true for all the elements in the stream. Here, it returns false as soon as it encounters 5, which is not divisible by 2.
        boolean oneEven = intList.stream().anyMatch(i -> i % 2 == 0);
        // anyMatch() checks if the predicate is true for any one element in the stream. Here, again short-circuiting is applied and true is returned immediately after the first element.
        boolean noneMultipleOfThree = intList.stream().noneMatch(i -> i % 3 == 0);
        // noneMatch() checks if there are no elements matching the predicate. Here, it simply returns false as soon as it encounters 6, which is divisible by 3.

        // assert
        assertFalse(allEven);
        assertTrue(oneEven);
        assertFalse(noneMultipleOfThree);
    }

    // Stream Specializations
    // From what we discussed so far, Stream is a stream of object references. However, there are also the IntStream, LongStream, and DoubleStream – which are primitive specializations for int, long and double respectively. These are quite convenient when dealing with a lot of numerical primitives.
    // These specialized streams do not extend Stream but extend BaseStream on top of which Stream is also built.
    // As a consequence, not all operations supported by Stream are present in these stream implementations. For example, the standard min() and max() take a comparator, whereas the specialized streams do not.

    /**
     * Creation
     * The most common way of creating an IntStream is to call mapToInt() on an existing stream.
     * <p>
     * Here, we start with a Stream<Employee> and get an IntStream by supplying the Employee::getId to mapToInt. Finally, we call max() which returns the highest integer.
     */
    @Test
    public void whenFindMaxOnIntStream_thenGetMaxInteger() {
        // act
        long maxId = this.empList.stream()
                .mapToLong(Employee::getId)
                .max()
                .orElseThrow(NoSuchElementException::new);

        // assert
        assertEquals(3L, maxId);
    }

    @Test
    public void differentWaysToGetStreamInt() {
        // We can also use IntStream.of() for creating the IntStream:
        IntStream intStream = IntStream.of(1, 2, 3);
        OptionalInt optionalIntMin = intStream.min();
        // assert
        assertTrue(optionalIntMin.isPresent());
        assertEquals(1, optionalIntMin.getAsInt());

        // Or, IntStream.range():
        IntStream rangeStream = IntStream.range(10, 20); // Which creates IntStream of numbers 10 to 19.
        OptionalInt optionalIntMax = rangeStream.max();
        // assert
        assertTrue(optionalIntMax.isPresent());
        assertEquals(19, optionalIntMax.getAsInt());

        // One important distinction to note before we move on to the next topic:
        // Bellow returns a Stream<Integer> and not IntStream.
        Stream<Integer> streamInt = Stream.of(1, 2, 3);
        Optional<Integer> optionalIntegerMin = streamInt.min(Comparator.comparingInt(Integer::intValue));
        // assert
        assertTrue(optionalIntegerMin.isPresent());
        assertEquals(new Integer(1), optionalIntegerMin.get());

        //Similarly, using map() instead of mapToInt() returns a Stream<Integer> and not an IntStream.:
        Stream<Long> streamLong = this.empList.stream().map(Employee::getId);
        Optional<Long> optionalIntegerMax = streamLong.max(Comparator.comparingLong(Long::longValue));
        // assert
        assertTrue(optionalIntegerMax.isPresent());
        assertEquals(new Long(3), optionalIntegerMax.get());
    }
}
