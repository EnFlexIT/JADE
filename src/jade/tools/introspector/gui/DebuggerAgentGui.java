/*Questa e' la finestra principale che contiene l-albero degli
agenti ed un DesktopPane al quale vengono aggiunte le finestre
della classe AgentDebugGui*/
/**
   @author Andrea Squeri, -  Universita` di Parma

*/



package jade.tools.debugger.gui;

import jade.gui.AgentTreeModel;
import jade.gui.AgentTree;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.util.*;
import jade.tools.debugger.DebuggerAgent;
import jade.core.AID;
import java.net.InetAddress;
import jade.tools.debugger.event.*;



public class DebuggerAgentGui extends JFrame implements WindowListener{

  private DebuggerAgent debugger;//il debugger

  /*pannelli*/
  private TreePanel panel;  //agentTree
  private JDesktopPane desk;//contnitore di internalFrame
  private JSplitPane split;
  private JPanel wind;
  private JScrollPane scroll;
  private JScrollPane scroll2;

  /*menu*/
  private JMenuBar bar;
  private JMenu menuFile;
  private JMenu menuHelp;
  private JMenuItem item;
  private JMenuItem about;

  public DebuggerAgentGui(DebuggerAgent d) {

    debugger = d;

    panel=new TreePanel(this.getDebuggerAgent(),this);
    panel.treeAgent.register("FIPAAGENT",new TreeAgentPopupMenu(d),"images/runtree.gif");
    panel.treeAgent.register("FIPACONTAINER",null,"images/TreeClosed.gif");

    /*pannelli*/
    scroll=new JScrollPane();
    scroll2=new JScrollPane();
    desk = new JDesktopPane();
    wind=new JPanel();
    split=new JSplitPane();

    /*menu*/
    bar = new JMenuBar();
    menuFile = new JMenu();
    item = new JMenuItem();
    menuHelp=new JMenu();
    about=new JMenuItem();


    /*costruisce l'interfaccia*/
    build();

  }

  public void build(){
    String title=debugger.getAID().getName();
    this.setTitle(title);
    Font f= new Font("Monospaced",0,10);

    this.setContentPane(wind);
    wind.setLayout(new BorderLayout());
    split.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    wind.add(split,BorderLayout.CENTER);


    /*menu*/
    menuFile.setText("File");
    item.setText("Esci");
    menuFile.add(item);
    bar.add(menuFile);
    this.setJMenuBar(bar);
    menuHelp.setText("Help");
    about.setText("About");
    menuHelp.add(about);
    about.setName("about");
    bar.add(menuHelp);

    item.addActionListener(new ActionListener()  {

      public void actionPerformed(ActionEvent e) {
        exit_actionPerformed(e);
      }
    });

    about.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent e) {
        about();
      }
    });

    scroll.getViewport().add(panel);
    scroll2.getViewport().add(desk);

    split.add(scroll,JSplitPane.LEFT);
    split.add(scroll2,JSplitPane.RIGHT);
    split.setDividerLocation(134);
    this.addWindowListener(this);

    try  {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch(Exception e) {
    }
    this.setSize(new Dimension(680, 435));
    setVisible(true);
  }


  void exit_actionPerformed(ActionEvent e){
    this.debugger.doDelete();
  }


  public JDesktopPane getDesktop(){
    return desk;
  }


  //interface WindowListener
  public void windowClosing(WindowEvent e){
    this.exit_actionPerformed(null);
  }
  public void windowClosed(WindowEvent e){}
  public void windowOpened(WindowEvent e){}
  public void windowIconified(WindowEvent e){}
  public void windowDeiconified(WindowEvent e){}
  public void windowDeactivated(WindowEvent e){}
  public void windowActivated(WindowEvent e){}

  public void about(){
    AboutBox about=new AboutBox(this,"About Jade DebuggerAgent",true);
    about.showCorrect();
  }



  ///////////////////////////////////////////////////
  //METODI CHIAMATI DA DEBUGGERAGENT ///////////////
  //////////////////////////////////////////////////
  public void messageEvent(MessageEvent e,MainWindow f){
    MessagePanel mp=f.getMessagePanel();
    EventQueue.invokeLater(new TableUpdater(e,mp));
  }
  public void stateEvent(StateEvent e,MainWindow f){
    StatePanel sp=f.getStatePanel();
    EventQueue.invokeLater(new StateUpdater(e,sp));
  }
  public void behaviourEvent(BehaviourEvent e,MainWindow f){
    BehaviourPanel bp=f.getBehaviourPanel();
     EventQueue.invokeLater(new TreeUpdater(e,bp));
  }

  //l'initEvent viene scomposto in tanti eventi
  //minori.Questo lo rende + lento ma meno complesso
  public void initEvent(InitEvent e,MainWindow f){

    Iterator it=e.getAllBehaviours();
    BehaviourPanel bp=f.getBehaviourPanel();
    while(it.hasNext()){
      BehaviourRapp b=(BehaviourRapp)it.next();
      BehaviourEvent event=new BehaviourEvent(b,false,true);
      EventQueue.invokeLater(new TreeUpdater(event,bp));
    }

    it=e.getAllinMessages();
    MessagePanel mp=f.getMessagePanel();
    while(it.hasNext()){
      MessageRapp b=(MessageRapp)it.next();
      MessageEvent event=new MessageEvent(b,true,true);
      EventQueue.invokeLater(new TableUpdater(event,mp));
    }

    it=e.getAlloutMessages();
    while(it.hasNext()){
      MessageRapp b=(MessageRapp)it.next();
      MessageEvent event=new MessageEvent(b,false,true);
      EventQueue.invokeLater(new TableUpdater(event,mp));
    }

    StateEvent se=new StateEvent(e.getState());
    StatePanel sp=f.getStatePanel();
    EventQueue.invokeLater(new StateUpdater(se,sp));
  }

  //aggiunge un InternalFrame(MainWindow)
  public void addWindow(MainWindow m){
    this.desk.add(m,null);
  }

  /*chiude la finstra principale*/
  public void disposeAsync() {
    class disposeIt implements Runnable {
      private Window toDispose;
      public disposeIt(Window w) {
	      toDispose = w;
      }
      public void run() {
	      toDispose.dispose();
      }
    }
    SwingUtilities.invokeLater(new disposeIt(this));
  }

  /*chiude un InternalFrame*/
  public void closeInternal(MainWindow m){
    class DisposeItMain implements Runnable{
      MainWindow wnd;
      DisposeItMain(MainWindow l){
        wnd=l;
      }
      public void run(){
        wnd.dispose();
      }
    }
    EventQueue.invokeLater(new DisposeItMain(m));
  }


  /////////////////////////////////////////////////////////
  /////METODI PER GESTIRE AGENTTREE ,PRESI DIRETTAMENTE
  /////DALLA GUI DELL' RMA
  ////////////////////////////////////////////////////////
  public void addAgent(final String containerName, final AID agentID) {

    // Add an agent to the specified container
    Runnable addIt = new Runnable() {
      public void run() {
	String agentName = agentID.getName();
       	AgentTree.Node node = panel.treeAgent.createNewNode(agentName, 1);
        panel.treeAgent.addAgentNode((AgentTree.AgentNode)node, containerName, agentName, "agentAddress", "FIPAAGENT");
      }
    };
    SwingUtilities.invokeLater(addIt);
  }

  public void removeAgent(final String containerName, final AID agentID) {

    // Remove an agent from the specified container
    Runnable removeIt = new Runnable() {
      public void run() {
	String agentName = agentID.getName();
	panel.treeAgent.removeAgentNode(containerName, agentName);
      }
    };
    SwingUtilities.invokeLater(removeIt);
  }
 public AgentTreeModel getModel() {
    return panel.treeAgent.getModel();
  }

  public void addContainer(final String name, final InetAddress addr) {
    Runnable addIt = new Runnable() {
      public void run() {
        MutableTreeNode node = panel.treeAgent.createNewNode(name, 0);
        panel.treeAgent.addContainerNode((AgentTree.ContainerNode)node,"FIPACONTAINER",addr);
      }
    };
    SwingUtilities.invokeLater(addIt);
  }

  public void removeContainer(final String name) {

    // Remove a container from the tree model
    Runnable removeIt = new Runnable() {

      public void run() {
       panel.treeAgent.removeContainerNode(name);
     }
    };
    SwingUtilities.invokeLater(removeIt);
  }


  public DebuggerAgent getDebuggerAgent(){
    return debugger;
  }



}



