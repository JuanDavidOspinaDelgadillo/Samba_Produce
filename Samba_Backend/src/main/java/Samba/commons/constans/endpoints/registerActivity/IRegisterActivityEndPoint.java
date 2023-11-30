package Samba.commons.constans.endpoints.registerActivity;

public interface IRegisterActivityEndPoint {
    String BASE_URL_REGISTER_ACTIVITY = "/register-activity";
    String CREATE_REGISTER_ACTIVITY = "/create";
    String READ_REGISTER_ACTIVITY = "/read{registerActivityId}";
    String READ_ALL_REGISTER_ACTIVITY = "/read-all";
    String READ_ALL_REGISTER_ACTIVITY_FOR_MACHINE = "/read-all-for-machine{machineSambaId}";
    String UPDATE_REGISTER_ACTIVITY = "/update";
    String DELETE_REGISTER_ACTIVITY = "/delete";
}