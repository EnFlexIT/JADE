package fipa.lang.acl;

// Classes representing different elements of ACL grammar

interface MessageParameter {

  public String value();

}


// A different class for each message parameter

class senderParam implements MessageParameter {

  public String value() {
    return null;
  }

}

class receiverParam implements MessageParameter {

  public String value() {
    return null;
  }

}

class contentParam implements MessageParameter {

  public String value() {
    return null;
  }

}

class replyWithParam implements MessageParameter {

  public String value() {
    return null;
  }

}

class replyByParam implements MessageParameter {

  public String value() {
    return null;
  }

}

class inReplyToParam implements MessageParameter {

  public String value() {
    return null;
  }

}

class envelopeParam implements MessageParameter {

  public String value() {
    return null;
  }

}

class languageParam implements MessageParameter {

  public String value() {
    return null;
  }

}

class ontologyParam implements MessageParameter {

  public String value() {
    return null;
  }

}

class protocolParam implements MessageParameter {

  public String value() {
    return null;
  }

}

class conversationIdParam implements MessageParameter {

  public String value() {
    return null;
  }

}


// To build complex expressions, defined by a recursive grammar, a
// Composite between ExprItem and Expression is used

interface ExprItem {
}

class WordItem implements ExprItem {
  // Component
}

class StringItem implements ExprItem {
  // Leaf
}

class NumberItem implements ExprItem {
  // Leaf
}

class Expression implements ExprItem {
  // Composite
}




