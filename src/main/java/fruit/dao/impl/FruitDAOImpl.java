package fruit.dao.impl;

import fruit.dao.FruitDAO;
import fruit.pojo.Fruit;
import myssm.basedao.BaseDAO;

import java.util.List;

public class FruitDAOImpl extends BaseDAO<Fruit> implements FruitDAO {
    @Override
    public List<Fruit> getFruitList(String keyword , Integer pageNo) {
        return super.executeQuery("select * from fruit_data where fname like ? or remark like ? limit ? , 5" ,"%"+keyword+"%","%"+keyword+"%", (pageNo-1)*5);
    }

    @Override
    public Fruit getFruitByFid(Integer fid) {
        return super.load("select * from fruit_data where fid = ? " , fid);
    }

    @Override
    public void updateFruit(Fruit fruit) {
        String sql = "update fruit_data set fname = ? , price = ? , fcount = ? , remark = ? where fid = ? " ;
        super.executeUpdate(sql,fruit.getFname(),fruit.getPrice(),fruit.getFcount(),fruit.getRemark(),fruit.getFid());
    }

    @Override
    public void delFruit(Integer fid) {
        super.executeUpdate("delete from fruit_data where fid = ? " , fid) ;
    }

    @Override
    public void addFruit(Fruit fruit) {
        String sql = "insert into fruit_data values(0,?,?,?,?)";
        super.executeUpdate(sql,fruit.getFname(),fruit.getPrice(),fruit.getFcount(),fruit.getRemark());
    }

    @Override
    public int getFruitCount(String keyword ) {
        return ((Long)super.executeComplexQuery("select count(*) from fruit_data where fname like ? or remark like ?" , "%"+keyword+"%","%"+keyword+"%")[0]).intValue();
    }
}
