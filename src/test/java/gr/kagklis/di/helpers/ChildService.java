package gr.kagklis.di.helpers;

import gr.kagklis.di.annotations.CustomComponent;
import gr.kagklis.di.components.MyService;

@CustomComponent
public class ChildService extends ParentService {

    public MyService exposeMyService() {
        return myService;
    }
}
