package br.edu.utfpr.butterfly.plugin;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import br.edu.utfpr.butterfly.model.Episode;
import br.edu.utfpr.butterfly.model.action.Action;
import br.edu.utfpr.butterfly.stream.EpisodeListener;

public class ButterflyView extends ViewPart implements EpisodeListener {

    public static final String ID = "br.edu.utfpr.butterfly.plugin.ButterflyView";

    private ProgrammingSession currentSession;

    private StartAction startAction;
    private StopAction stopAction;

    private Label statusLabel;
    private TreeViewer viewer;

    public static ButterflyView sharedInstance;

    public static ButterflyView getInstance() {
        return sharedInstance;
    }

    public ButterflyView() {
        super();
        ButterflyView.sharedInstance = this;
    }

    private final class StopAction extends org.eclipse.jface.action.Action {

        public StopAction() {
            setText("Stop");
            setImageDescriptor(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/nav_stop.gif"));
        }

        public void run() {
            if (currentSession != null) {
                currentSession.close();
            }
            viewer.setInput(null);

            setEnabled(false);
            startAction.setEnabled(true);

            statusLabel.setText("Stopped");
            statusLabel.getParent().layout();
        }
    }

    private final class StartAction extends org.eclipse.jface.action.Action {

        private static final String MESSAGE_SELECT_PROJECT = "Please, select a project or a resource in package explorer";

        public StartAction() {
            setText("Start");
            setImageDescriptor(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/start_task.gif"));
        }

        public void run() {
            File projectRootDir = null;
            String projectName = null;

            IEditorPart editorPart = getSite().getWorkbenchWindow().getActivePage().getActiveEditor();
            if (editorPart != null) {
                IFileEditorInput input = (IFileEditorInput) editorPart.getEditorInput();
                IFile file = input.getFile();
                IProject activeProject = file.getProject();
                projectRootDir = activeProject.getLocation().toFile();
                projectName = activeProject.getName();
            }

            System.out.println("ProjectRootDir: " + projectRootDir);
            System.out.println("ProjectRootName: " + projectName);

            if (projectRootDir != null) {

                currentSession = ProgrammingSession.newSession(projectRootDir);
                currentSession.addEpisodeListeners(ButterflyView.this);
                viewer.setInput(currentSession.getEpisodes());

                viewer.getControl().setMenu(new DisagreementPopupMenu(viewer, currentSession).getMenu());

                currentSession.start();

                stopAction.setEnabled(true);
                setEnabled(false);

                statusLabel.setText("Recording " + projectName);
                statusLabel.getParent().layout();
            } else {
                MessageDialog.openInformation(viewer.getControl().getShell(),
                        "Warning", MESSAGE_SELECT_PROJECT);
            }
        }
    }

    class ViewContentProvider implements ITreeContentProvider {

        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }

        public void dispose() {
        }

        public Object[] getElements(Object parent) {
            if (currentSession != null) {
                return currentSession.getEpisodes();
            } else {
                return null;
            }
        }

        public Object[] getChildren(Object parentElement) {

            if (parentElement instanceof Episode) {
                return ((Episode) parentElement).getActions().toArray();
            } else if (parentElement instanceof Action) {
                return ((Action) parentElement).getActionDetails().toArray();
            }
            return null;
        }

        public Object getParent(Object element) {
            return null;
        }

        public boolean hasChildren(Object element) {
            if (element instanceof Episode) {
                return ((Episode) element).getActions().size() > 0;
            } else if (element instanceof Action) {
                return ((Action) element).getActionDetails().size() > 0;
            }
            return false;
        }
    }

    class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {

        public String getColumnText(Object obj, int index) {
            return getText(obj);
        }

        public Image getColumnImage(Object obj, int index) {
            return getImage(obj);
        }

        public Image getImage(Object obj) {
            if (obj instanceof Episode) {

                Episode episode = (Episode) obj;

                String imgFileName = getIcon(episode);

                return Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, imgFileName).createImage();
            } else if (obj instanceof Action) {
                return Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/action.gif").createImage();
            } else if (obj instanceof String) {
                return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
            } else {
                return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
            }
        }

        private String getIcon(Episode episode) {
            String imgFileName = "icons/";

            if (episode.isTdd() == null) {
                imgFileName += "episode";
            } else if (episode.isTdd()) {
                imgFileName += "episode_conformant";
            } else {
                imgFileName += "episode_nonconformant";
            }

            if (episode.isDisagree()) {
                imgFileName += "_disagree";
            }

            imgFileName += ".png";
            return imgFileName;
        }
    }

    @Override
    public void createPartControl(Composite parent) {

        GridLayout layout = new GridLayout();
        parent.setLayout(layout);

        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.verticalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;

        GridData labelGridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;

        statusLabel = new Label(parent, SWT.NONE);
        statusLabel.setText("Stopped");
        statusLabel.setLayoutData(labelGridData);

        viewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
        viewer.setContentProvider(new ViewContentProvider());
        viewer.setLabelProvider(new ViewLabelProvider());
        viewer.getControl().setLayoutData(gridData);

        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager manager = bars.getToolBarManager();

        startAction = new StartAction();
        stopAction = new StopAction();

        stopAction.setEnabled(false);
        startAction.setEnabled(true);

        manager.add(startAction);
        manager.add(stopAction);
    }

    @Override
    public void setFocus() {
    }

    public void episodeRecognized(final Episode e) {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                viewer.refresh();
            }
        });
    }

}
