package gr.kagklis.di.components;

import gr.kagklis.di.annotations.CustomAutowired;
import gr.kagklis.di.annotations.CustomComponent;

@CustomComponent
public class OtherService {

    @CustomAutowired
    AnotherService anotherService;

    @CustomAutowired
    YetAnotherService yetAnotherService;
}
