package gr.kagklis.di;

import gr.kagklis.di.annotations.CustomAutowired;
import gr.kagklis.di.exceptions.AbstractCreationNotAllowedException;
import gr.kagklis.di.exceptions.CircularDependencyException;
import gr.kagklis.di.exceptions.NoImplementationFoundException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.Map.Entry;

public class DependencyInjector {
    Map<Class<?>, Object> singletonsMap;
    DIMapWrapper diMapWrapper;

    public DependencyInjector() {
        singletonsMap = new HashMap<>();
        diMapWrapper = new DIMapWrapper();
    }

    @SuppressWarnings("unchecked")
    public <T> T singletonOf(Class<T> requestedClass) {
        if (singletonsMap.containsKey(requestedClass)) {
            return (T) singletonsMap.get(requestedClass);
        }

        T result = createObject(requestedClass);
        singletonsMap.put(requestedClass, result);
        return result;
    }

    public <T> T oneOf(Class<T> requestedClass) {
        return createObject(requestedClass);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> listOf(Class<T> interfaceClass) {
        List<T> result = new ArrayList<>();
        Set<Entry<Class<?>, Class<?>>> implementationClasses =
                diMapWrapper.findAllInterfaceImplementationClasses(interfaceClass);

        if (implementationClasses.isEmpty()) {
            throw new NoImplementationFoundException(interfaceClass.getTypeName());
        }

        for (Entry<Class<?>, Class<?>> entry : implementationClasses) {
            result.add(createObject((Class<T>) entry.getKey()));
        }
        return result;
    }

    private <T> T createObject(Class<T> clazz) {
        return createObject(clazz, null);
    }

    private <T> T createObject(Class<T> givenClass, HashSet<String> dependencies) {
        Class<T> resolvedClass = diMapWrapper.findImplementationClass(givenClass);
        checkClassNotAbstract(resolvedClass);
        dependencies = processDependencies(givenClass, dependencies);
        return instantiateAndAutowire(resolvedClass, dependencies);
    }

    private void checkClassNotAbstract(Class<?> resolvedClass) {
        if (Modifier.isAbstract(resolvedClass.getModifiers())) {
            throw new AbstractCreationNotAllowedException(resolvedClass.getTypeName());
        }
    }

    private <T> HashSet<String> processDependencies(Class<T> givenClass, HashSet<String> dependencies) {
        // Initialize dependency list.
        if (dependencies == null) {
            dependencies = new HashSet<>();
        }

        // Check for circular dependencies.
        String givenClassTypeName = givenClass.getTypeName();
        if (dependencies.contains(givenClassTypeName)) {
            throw new CircularDependencyException(givenClassTypeName);
        }

        // Add current class to dependencies.
        dependencies.add(givenClassTypeName);
        return dependencies;
    }

    private <T> T instantiateAndAutowire(Class<T> resolvedClass, HashSet<String> dependencies) {
        T resultObject = null;
        try {
            resultObject = resolvedClass.newInstance();
            autowire(resolvedClass, resultObject, dependencies);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    private void autowire(Class<?> actualClass, Object classInstance, HashSet<String> dependencies)
            throws InstantiationException, IllegalAccessException {
        Set<Field> fields = findAutowiredFields(actualClass);
        boolean hasMultipleFields = fields.size() > 1;
        for (Field field : fields) {
            HashSet<String> branchDependencies = hasMultipleFields ? new HashSet<>(dependencies) : dependencies;
            Object fieldInstance = createObject(field.getType(), branchDependencies);
            field.set(classInstance, fieldInstance);
        }
    }

    private Set<Field> findAutowiredFields(Class<?> givenClass) {
        Set<Field> results = new HashSet<>();
        while (givenClass != null) {
            for (Field field : givenClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(CustomAutowired.class)) {
                    field.setAccessible(true);
                    results.add(field);
                }
            }
            givenClass = givenClass.getSuperclass();
        }
        return results;
    }

}
