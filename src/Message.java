import java.io.Serializable;


public class Message implements Serializable {
  static final int message = 1, getOnlineUsers = 2, logout = 3;
  private final int type;
  private final String content;
//  private int receiverId;

  Message(int type, String content) {
    this.type = type;
    this.content = content;
  }

  int getType() {
    return type;
  }

  String getContent() {
    return content;
  }

//  int getReceiverId() {
//    return receiverId;
//  }
}
