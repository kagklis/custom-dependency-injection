package gr.kagklis.di.components;

import gr.kagklis.di.annotations.CustomAutowired;
import gr.kagklis.di.annotations.CustomComponent;

@CustomComponent
public class CarInsuranceProvider2 implements CarInsuranceProvider {

    @CustomAutowired
    MyService myService;
}
