package com.mucommander.commons.file.protocol.smb;

public interface SmbjLogic<T> {

    T doLogic(SmbjConnectionHandler connectionHandler) throws Exception;

}
