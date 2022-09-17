import java.util.ArrayList;
import java.util.HashMap;

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
        System.out.println("\n");
    }
}
