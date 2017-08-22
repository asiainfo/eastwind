package eastwind.io2;

import eastwind.io.serializer.SerializerFactoryHolder;

public abstract class AbstractConnector implements Connector {

    protected SerializerFactoryHolder serializerFactoryHolder = new SerializerFactoryHolder();

    protected int masterThreads;
    protected int workerThreads;

    public AbstractConnector(int masterThreads, int workerThreads) {
        this.masterThreads = masterThreads;
        this.workerThreads = workerThreads;
    }

    public int getMasterThreads() {
        return masterThreads;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }

    public SerializerFactoryHolder getSerializerFactoryHolder() {
        return serializerFactoryHolder;
    }
}
