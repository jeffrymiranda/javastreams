package com.example.streams.domain;

import com.example.streams.repository.EmployeeRepository;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
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

    private static Employee[] arrayOfEmps = {
            new Employee(1L, "Jeff Bezos", 100_000.0),
            new Employee(2L, "Bill Gates", 200_000.0),
            new Employee(3L, "Mark Zuckerberg", 300_000.0)
    };

    private static List<Employee> empList = Arrays.asList(arrayOfEmps);

    @Test
    public void mustResolveStreamCreation() {
        //act
        // Let’s first obtain a stream from an existing array:
        Stream<Employee> employeeStream1 = Stream.of(arrayOfEmps);
        // We can also obtain a stream from an existing list:
        Stream<Employee> employeeStream2 = empList.stream();
        // And we can create a stream from individual objects using Stream.of():
        Stream<Employee> employeeStream3 = Stream.of(arrayOfEmps[0], arrayOfEmps[1], arrayOfEmps[2]);
        // Or simply using Stream.builder():
        Stream.Builder<Employee> empStreamBuilder = Stream.builder();

        empStreamBuilder.accept(arrayOfEmps[0]);
        empStreamBuilder.accept(arrayOfEmps[1]);
        empStreamBuilder.accept(arrayOfEmps[2]);

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
        // empList.stream().forEach(e -> e.salaryIncrement(10.0));
        // The forEach is so common that is has been introduced directly in List, Iterable, Map etc:
        empList.forEach(e -> e.salaryIncrement(10.0));

        // assert
        assertThat(empList, contains(
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
        List<Employee> employees = empList.stream().collect(Collectors.toList());

        assertEquals(empList, employees);
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
        Employee result = empList
                .stream()
                .map(Employee::getId)
                .map(this.employeeRepository::findById)
                .filter(employee -> employee.getSalary() > 100_000)
                .findFirst()
                .orElse(null);

        // assert
        assertNotNull(result);
        assertEquals(result.getFullName(), "Bill Gates");
    }

    /**
     * toArray
     * We saw how we used collect() to get data out of the stream. If we need to get an array out of the stream, we can simply use toArray().
     * <p>
     * The syntax Employee[]::new creates an empty array of Employee – which is then filled with elements from the stream.
     */
    @Test
    public void whenStreamToArray_thenGetArray() {
        // arrange
        Employee[] employees = empList.stream().toArray(Employee[]::new);

        // assert
        assertThat(empList.toArray(), equalTo(employees));
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
     * peek() can be useful in situations like this. Simply put, it performs the specified operation on each element of the stream and returns a new stream which can be used further. peek() is an intermediate operation:
     */
    @Test
    public void whenIncrementSalaryUsingPeek_thenApplyNewSalary() {
        // act
        empList.stream()
                .peek(employee -> employee.salaryIncrement(15.0))
                .peek(EmployeeTest::printNewSalary)
                .collect(Collectors.toList());

        // assert
        assertThat(empList, contains(
                hasProperty("salary", equalTo(115_000.0)),
                hasProperty("salary", equalTo(230_000.0)),
                hasProperty("salary", equalTo(345_000.0))
        ));
    }

    private static void printNewSalary(Employee employee) {
        System.out.println(employee.getFullName() + "' new salary: " + employee.getSalary());
    }

}