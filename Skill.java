import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class Skill 
{
    private long lvl;
    private String name;

    private Skill root;
    private ArrayList<Skill> stems;


    public Skill() 
    {
        lvl = 1;
        name = "Do Something";

        root = null;
        stems = new ArrayList<Skill>();
    }

    public Skill(long skillLevel, String skillName, Skill skillRoot) 
    {
        lvl = skillLevel;
        name = skillName;

        root = skillRoot;
        stems = new ArrayList<Skill>();
    }


    public long getLevel()
    {
        return lvl;
    }

    public String getName()
    {
        return name;
    }

    public Skill getRoot()
    {
        return root;
    }

    public void stemSkill(String newSkill)
    {
        Skill stemmed = new Skill(this.lvl + 1, newSkill, this);
        stems.add(stemmed);
    }

    public ArrayList<Skill> getStems()
    {
        return stems;
    }

    public Skill searchSkills(String skillName)
    {   
        if (this.name.toLowerCase().equals(skillName.toLowerCase()))
            return this;
        else
        {
            int resultPool = this.stems.size();

            if (resultPool > 0)
            {
                Skill[] results = new Skill[resultPool];
                for (int i = 0; i < resultPool; i++)
                    results[i] = this.stems.get(i).searchSkills(skillName);

                Skill retVal = null;
                for (int i = 0; i < resultPool; i++)
                    if (results[i] != null)
                        retVal = results[i];

                return retVal;
            }
            else
                return null;
        }
    }

    public void printTree()
    {
        for (int i = 0; i < lvl - 1; i++)
            System.out.print(" ");
        
        System.out.println("> Lvl." + lvl + ": " + name);

        for (Skill s : stems)
            s.printTree();
    }

    @SuppressWarnings("unchecked")
    public JSONObject jsonify()
    {
        JSONObject jRet = new JSONObject();

        jRet.put("level", lvl);
        jRet.put("skill", name);
        
        String prevVal;
        if (root == null)
            prevVal = "N/a";
        else
            prevVal = root.getName();
            
        jRet.put("previous", prevVal);

        return jRet;
    }

    @SuppressWarnings("unchecked")
    public static void scanTree(JSONArray treeStruct, Skill current)
    {
        JSONObject node = current.jsonify();

        treeStruct.add(node);

        for (Skill s : current.getStems())
            Skill.scanTree(treeStruct, s);
    }
}
