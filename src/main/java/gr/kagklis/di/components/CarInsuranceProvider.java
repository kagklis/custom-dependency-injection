package gr.kagklis.di.components;

import gr.kagklis.di.annotations.CustomQualifier;

@CustomQualifier(value = "gr.kagklis.di.components.CarInsuranceProvider1")
public interface CarInsuranceProvider {

    String getSomething();
}
