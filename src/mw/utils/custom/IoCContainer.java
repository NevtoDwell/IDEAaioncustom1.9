package mw.utils.custom;

import java.util.HashMap;
import java.util.Map;

/**
 * Basic IoC container abstraction
 */
public abstract class IoCContainer {

    /* Instances holder */
    private Map<Class, Binder<?>> _classes = new HashMap<>();

    /* Creates binder of selected class*/
    protected  <T> Binder<T> bind(Class<T> binder){
        return new Binder<>(this, binder);
    }

    protected <T> T resolve(Class<T> clazz){

        Binder result = _classes.get(clazz);
        if(result == null)
            throw new Error("No class binded to source class " + clazz.getName());

        return (T)result._instance;
    }

    /* Information about object class to bind to instance*/
    protected static class Binder<T>{

        /* Binder owner container*/
        private IoCContainer _owner;

        /* Class to bind to */
        private Class<T> _binder;

        /* Object instance */
        private T _instance;

        /**
         * Default hidden constructor to ensure custom instancing
         *
         * @param owner Owner container instance
         */
        private Binder(IoCContainer owner, Class<T> binder){
            _owner = owner;
            _binder = binder;
        }

        /**
         * Bind new instance of class, that inherited binter type
         * @param clazz target class with parameterless constructor
         *
         * @throws IllegalAccessException
         * @throws InstantiationException
         */
        public void to(Class<? extends T> clazz) throws IllegalAccessException, InstantiationException {

            _instance = clazz.newInstance();
            _owner._classes.put(_binder, this);
        }
    }
}
