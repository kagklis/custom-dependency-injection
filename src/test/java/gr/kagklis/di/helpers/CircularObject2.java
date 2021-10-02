package gr.kagklis.di.helpers;

import gr.kagklis.di.annotations.CustomAutowired;
import gr.kagklis.di.annotations.CustomComponent;

@CustomComponent
public class CircularObject2 {

    @CustomAutowired
    CircularObject1 object1;
}
