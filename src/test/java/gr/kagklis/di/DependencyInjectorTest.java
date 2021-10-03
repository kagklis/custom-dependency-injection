package gr.kagklis.di;

import gr.kagklis.di.components.CarInsuranceProvider;
import gr.kagklis.di.components.MyService;
import gr.kagklis.di.exceptions.*;
import gr.kagklis.di.helpers.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DependencyInjectorTest {

    private static DependencyInjector di;

    @BeforeAll
    public static void init() {
        di = new DependencyInjector();
    }

    @Test
    public void testSingletonOf() {
        MyService ref1 = di.singletonOf(MyService.class);
        MyService ref2 = di.singletonOf(MyService.class);

        assertNotNull(ref1, "MyService was not created");
        assertNotNull(ref2, "MyService was not created");
        assertEquals(ref1, ref2);
    }

    @Test
    public void testOneOf() {
        MyService ref = di.oneOf(MyService.class);

        assertNotNull(ref, "MyService is null");
    }

    @Test
    public void testListOf() {
        List<CarInsuranceProvider> implementations = di.listOf(CarInsuranceProvider.class);

        assertNotNull(implementations, "List of implementations was not created");
        assertEquals(2, implementations.size());
    }

    @Test
    public void testAbstractClass() {
        assertThrows(AbstractCreationNotAllowedException.class, () -> di.oneOf(AbstractClass.class));
    }

    @Test
    public void testAbstractClassThroughInterface() {
        assertThrows(AbstractCreationNotAllowedException.class, () -> di.oneOf(InterfaceOfAbstractClass.class));
    }

    @Test
    public void testCircularDependencies() {
        assertThrows(CircularDependencyException.class, () -> di.oneOf(CircularObject1.class));
    }

    @Test
    public void testInterfaceWithQualifier() {
        CarInsuranceProvider ref = di.oneOf(CarInsuranceProvider.class);

        assertNotNull(ref, "CarInsuranceProvider is null");
    }

    @Test
    public void testInterfaceWithoutQualifier() {
        assertThrows(MoreThanOneImplementationFoundException.class, () -> di.oneOf(InterfaceWithoutQualifier.class));
    }

    @Test
    public void testInterfaceWithInvalidQualifier() {
        assertThrows(InvalidQualifierValueFoundException.class, () -> di.oneOf(InterfaceWithInvalidQualifier.class));
    }

    @Test
    public void testInterfaceWithNoImplementation() {
        assertThrows(NoImplementationFoundException.class, () -> di.oneOf(InterfaceWithNoImplementation.class));
    }

    @Test
    public void testClassNotMarkedAsComponent() {
        assertThrows(ComponentNotFoundException.class, () -> di.oneOf(NotMarkedAsComponent.class));
    }

    @Test
    public void testParentClassDependencies() {
        ChildService childService = di.oneOf(ChildService.class);

        assertNotNull(childService.exposeMyService());
    }

}
