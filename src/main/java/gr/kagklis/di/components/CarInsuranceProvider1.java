package gr.kagklis.di.components;

import gr.kagklis.di.annotations.CustomAutowired;
import gr.kagklis.di.annotations.CustomComponent;

@CustomComponent
public class CarInsuranceProvider1 implements CarInsuranceProvider {

    @CustomAutowired
    MyService myService;

    @Override
    public String getSomething() {
        return "Provider1";
    }
}
