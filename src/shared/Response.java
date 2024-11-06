package shared;

public interface Response extends Sendable{

    int code = 0;
    String status = "";


    public int getCode() ;
    public String getStatus() ;


}
