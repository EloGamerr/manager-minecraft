package fr.elogamerr.manager.files;

abstract class FileObject {
    protected abstract void preLoad();

    protected abstract void load();

    protected void postLoad() {

    }

    protected abstract void save();
}
