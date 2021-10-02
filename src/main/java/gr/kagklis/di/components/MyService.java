package gr.kagklis.di.components;

import gr.kagklis.di.annotations.CustomAutowired;
import gr.kagklis.di.annotations.CustomComponent;

@CustomComponent
public class MyService {

    @CustomAutowired
    OtherService otherService;

    @CustomAutowired
    YetAnotherService yetAnotherService;
}
