package fipa.core;

import java.rmi.RemoteException;

class NotFoundException externds RemoteException {

  NotFoundException(String msg) {
    super(msg);
  }
}
