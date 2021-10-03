package gr.kagklis.di.helpers;

import gr.kagklis.di.annotations.CustomAutowired;
import gr.kagklis.di.annotations.CustomComponent;
import gr.kagklis.di.components.MyService;

@CustomComponent
public class ParentService {

    @CustomAutowired
    MyService myService;
}
