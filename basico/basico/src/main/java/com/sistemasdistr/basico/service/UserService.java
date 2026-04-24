public class UserService {

    @Autowired
    private UserRepository repo;

    @Autowired
    private KeyService keyService;

    public User register(String username, String password) throws Exception {


        Map<String, String> keys = keyService.generateKeyPair();

        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // hash in real app
        user.setPublicKey(keys.get("public"));
        user.setPrivateKey(keys.get("private"));

        return repo.save(user);
    }
}