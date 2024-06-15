package me.gravityio.easyrename.mixins.inter;

public interface INameableScreen {
    void easyRename$setNameable(boolean n);

    boolean easyRename$isNameable();
    void easyRename$onResponse(boolean success);

}
