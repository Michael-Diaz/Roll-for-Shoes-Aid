import java.util.ArrayList;

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
        Skill retSkill = null;
        
        if (this.name.toLowerCase().equals(skillName.toLowerCase()))
            retSkill = this;
        else
        {
            for (Skill s : stems)
                retSkill = s.searchSkills(skillName);
        }

        return retSkill;
    }

    public void printTree()
    {
        for (int i = 0; i < lvl - 1; i++)
            System.out.print(" ");
        
        System.out.println("> " + lvl + ": " + name);

        for (Skill s : stems)
            s.printTree();
    }
}
