package gr.kagklis.di.components;

import gr.kagklis.di.DependencyInjector;
import gr.kagklis.di.exceptions.*;
import gr.kagklis.di.helpers.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestDependencyInjector {

    private static DependencyInjector di;

    @BeforeAll
    public static void init() {
        di = new DependencyInjector();
    }

    @Test
    public void testSingletonOf() {
        MyService ref1 = di.singletonOf(MyService.class);
        MyService ref2 = di.singletonOf(MyService.class);

        assertNotNull(ref1, "ref1 is null");
        assertNotNull(ref2, "ref2 is null");
        assertEquals(ref1, ref2);
        assertNotNull(ref1.otherService);
        assertNotNull(ref1.otherService.anotherService);
        assertNotNull(ref1.otherService.yetAnotherService);
        assertNotNull(ref1.yetAnotherService);
    }

    @Test
    public void testOneOf() {
        MyService myService = di.oneOf(MyService.class);

        assertNotNull(myService, "myService is null");
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
        CarInsuranceProvider provider = di.oneOf(CarInsuranceProvider.class);

        assertNotNull(provider, "provider is null");
        assertEquals("Provider1", provider.getSomething());
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
