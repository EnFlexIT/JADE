package fipa.core;

import java.rmi.RemoteException;

class NotFoundException extends RemoteException {

  NotFoundException(String msg) {
    super(msg);
  }
}
