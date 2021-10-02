package gr.kagklis.di.helpers;

import gr.kagklis.di.annotations.CustomAutowired;
import gr.kagklis.di.annotations.CustomComponent;

@CustomComponent
public class CircularObject1 {

    @CustomAutowired
    CircularObject2 object2;
}
