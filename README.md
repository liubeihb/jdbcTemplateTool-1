# jdbc-template-tool
this is a jdbc template enhancement with general save() update() list() method, and auto generate sql with object .
there are some example
    
    @Test()
    public void save() throws Exception {
        User user = new User();
        user.setId("2");
        user.setName("cloud2");
        user.setAge(new Random().nextInt(100));
        jtt.save(user);
    }
    
    @Test()
    public void updateByObject() throws Exception {
        jtt.execute("insert into t_user(id,name,age) values ('3','cloud3','34')", null);
        /**
         * 能够根据对象的值自动生成update 语句，id为必填项,null值自动忽略
         *  @Id
         *  public String getId() 
         *  
         * real sql: update t_user set dob = ? where id = ?
         */
        User user = new User();
        user.setId("3");
        user.setDob(new Date());
        jtt.update(user);
    }
    
    
    @Test()
    public void list() throws Exception {
        jtt.execute("insert into t_user(id,name,age) values ('4','cloud4','34')", null);
        List<User> list = jtt.list("select * from t_user where name like ? limit ?,?", new Object[]{"%cloud%",1,2}, User.class);
        for (User user : list) {
            System.out.println(user.getName());
        }
    }
    
    @Test()
    public void listByObjectAndPagination() throws Exception {
        /**
         * 能够根据对象的值自动生成select 语句，并且可以根据@Operator注解自定义运算符,null值自动忽略
         *  @Operator(value="like")
         *  public String getName()
         *  @Transient
         *  @Operator(targetColumn="age",value=">=")
         *  public Integer getAgeStart()
         *  @Transient
         *  @Operator(targetColumn="age",value="<=")
         *  public Integer getAgeEnd()
         * 
         * real sql:select * from .t_user where 1=1  and name like ?  and age >= ?  and age <= ?  limit 2,1
         */
        jtt.execute("insert into t_user(id,name,age) values ('5','cloud5','34')", null);
        User user = new User();
        user.setName("%cloud%");
        user.setAgeStart(20);
        user.setAgeEnd(50);
        List<User> list = jtt.listWithPagination(user, 2, 1);
        for (User u : list) {
            System.out.println("name: "+u.getName()+" age: "+u.getAge());
        }
    }
    
    @Test()
    public void in() throws Exception {
        jtt.execute("delete from t_user", null);
        jtt.execute("insert into t_user(id,name,age) values ('6','cloud6',34)", null);
        jtt.execute("insert into t_user(id,name,age) values ('7','cloud7',35)", null);
        jtt.execute("insert into t_user(id,name,age) values ('8','cloud8',36)", null);
        String[] parms = new String[3];
        parms[0] = "6";
        parms[1] = "7";
        parms[2] = "8";
        UserInSearch user1 = new UserInSearch();
        user1.setId(InUtils.getStr4SQLINParam(parms));
        List<User> list1 = jtt.list(user1);
        for (User u : list1) {
            System.out.println("name: "+u.getName()+" age: "+u.getAge());
        }
        
        List<Integer> listParms = new ArrayList<Integer>();
        listParms.add(34);
        listParms.add(35);
        listParms.add(36);
        UserInSearch user2 = new UserInSearch();
        user2.setAgeIn(InUtils.getStr4SQLINParam(listParms));
        List<User> list2 = jtt.list(user2);       
        for (User u : list2) {
            System.out.println("name: "+u.getName()+" age: "+u.getAge());
        }
        
    }
