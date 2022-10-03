import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class PlayerCharacter
{
    private String charName;
    public ArrayList<String> charPronouns;

    private long charXp;
    private Skill skillTreeRoot;

    private HashMap<String, Long> charInv;

    PlayerCharacter(ArrayList<String> ids, long xp, Skill root, HashMap<String, Long> inv)
    {
        charName = ids.get(0);
        ids.remove(0);

        charPronouns = ids;

        charXp = xp;
        skillTreeRoot = root;

        charInv = inv;
    }

    public String getName()
    {
        return charName;
    }

    public long getXP()
    {
        return charXp;
    }

    public void alterXP(long val)
    {
        charXp += val;
    }

    public Skill getSkillRoot()
    {
        return skillTreeRoot;
    }

    public HashMap<String, Long> getInventory()
    {
        return charInv;
    }

    public void displayStats()
    {
        String header = "===== " + charName + " " + charPronouns.toString().toUpperCase() + " =====\n";
        System.out.println(header);

        System.out.println("Available XP: " + charXp);
        System.out.println("Skills: \n-------");
        skillTreeRoot.printTree();

        System.out.println("\nInventory: ");
        if (charInv.isEmpty())
            System.out.println("  -Empty!");
        else
        {
            for (String key: charInv.keySet())
            {
                String[] itemDescs = key.split("~");
                long amt = charInv.get(key);

                System.out.println("  -" + itemDescs[0] + " (x" + amt + "): " + itemDescs[1]);
            }
        }

        System.out.print("\n");
        for (int i = 0; i < header.length() - 1; i++)
            System.out.print("=");
    }

    @SuppressWarnings("unchecked")
    public JSONObject jsonify()
    {
        JSONObject jRet = new JSONObject();

        jRet.put("name", charName);
        jRet.put("pronouns", charPronouns);
        jRet.put("xp", charXp);

        JSONArray jSkillTree = new JSONArray();
        Skill.scanTree(jSkillTree, skillTreeRoot);
        jRet.put("skills", jSkillTree);

        JSONArray jInv = new JSONArray();
        for (String key: charInv.keySet())
        {
            JSONObject jItem = new JSONObject();

            String[] itemDescs = key.split("~");
            long amt = charInv.get(key);

            jItem.put("item", itemDescs[0]);
            jItem.put("description", itemDescs[1]);
            jItem.put("amount", amt);

            jInv.add(jItem);
        }
        jRet.put("inventory", jInv);

        return jRet;
    }
}
