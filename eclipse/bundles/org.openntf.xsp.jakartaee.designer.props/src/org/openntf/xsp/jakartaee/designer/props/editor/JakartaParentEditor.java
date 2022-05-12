/*
 * � Copyright IBM Corp. 2011
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package org.openntf.xsp.jakartaee.designer.props.editor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertiesFileEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.openntf.xsp.jakartaee.designer.props.JakartaDesignPropsBean;
import org.openntf.xsp.jakartaee.designer.props.JakartaPropBean;
import org.openntf.xsp.jakartaee.designer.props.XSPPropBeanLoader;
import org.openntf.xsp.jakartaee.designer.props.pages.MainPage;

import com.ibm.commons.iloader.node.DataNode;
import com.ibm.commons.iloader.node.DataNodeAdapter;
import com.ibm.commons.iloader.node.IClassDef;
import com.ibm.commons.iloader.node.NodeException;
import com.ibm.commons.iloader.node.collections.SingleCollection;
import com.ibm.commons.iloader.node.loaders.JavaBeanLoader;
import com.ibm.commons.swt.data.controls.DCPanel;
import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.ide.resources.extensions.DesignerProject;
import com.ibm.designer.domino.ui.commons.extensions.DesignerResource;
import com.ibm.domino.xsp.module.nsf.NSFComponentModule;

@SuppressWarnings("restriction")
public class JakartaParentEditor extends MultiPageEditorPart implements ISelectionProvider {
    private FileEditorInput dbPropInput = null;
    private IProject project;
    private IFile xspDesignProps;
    private JakartaDesignPropsBean xspDesignPropsBean;
    private JakartaPropBean dbBean = null;
    private DesignerProject dp;
    private XSPPropBeanLoader jbl = null;
    private JavaBeanLoader xspDesignLoader;
    private IClassDef dbPropClassDef = null;
    private IClassDef xspDesignPropsClassDef;
    protected ISelection editorSelection = null;
    private FormToolkit toolkit = null;
    private Collection<DCPanel> pages = new ArrayList<>();
    private PropertiesFileEditor pfe = null;
    private int GENERAL_TAB = 0;
    private int sourcePageNum = 0;
    private int curPage = 0;
    private final String DESIGN_PATH = "AppProperties/xspdesign.properties"; //$NON-NLS-1$
    
    @Override
    public void doSave(IProgressMonitor progress) {
        if (isDirty()) {
            // save the contents of the source editor, always
            pfe.doSave(progress);
            dbBean.save(progress);  // no real saving here, but some necessary cleanup
            xspDesignPropsBean.save(progress);
            this.pages.forEach(page -> page.getDataNode().setModelModified(false));
            setModified(false);
            
            NSFComponentModule.setLastDesignerSave(System.currentTimeMillis()); 
        }
    }

    @Override
    public void doSaveAs() {
    }
    
    @Override
    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {
        setSite(site);
        setInput(input);
        String ns = "xspdbjbl"; //$NON-NLS-1$
        if (jbl == null) {
            jbl = new XSPPropBeanLoader(ns);
        }
        ns = "xspdesignjbl"; //$NON-NLS-1$
        xspDesignLoader = new JavaBeanLoader(ns);

        pfe = new PropertiesFileEditor();

        dbPropInput = (FileEditorInput)input;
        if (dbPropInput != null) {
            project = getDesignerProject();
            dp = (DesignerProject)Platform.getAdapterManager().getAdapter(project, DesignerProject.class);
            if(project != null){
                try {
                    IFile f = project.getFile(DESIGN_PATH);
                    f.getParent().refreshLocal(IFile.DEPTH_ONE, new NullProgressMonitor());
                    if(!f.exists()){
                        f.getParent().refreshLocal(IFile.DEPTH_ONE, new NullProgressMonitor());
                        f.create(new ByteArrayInputStream("".getBytes()), true, new NullProgressMonitor()); //$NON-NLS-1$
                    }
                    if(f.exists()){
                        xspDesignProps = f;
                        xspDesignPropsBean = new JakartaDesignPropsBean(dp, xspDesignProps);
                    }
                } catch (CoreException e) {
                    e.printStackTrace();
                }
            }
            
            dbBean = new JakartaPropBean(dp, getBeanLoader(), this, dbPropInput, xspDesignPropsBean);
            jbl.setPropFile(dbPropInput, this);
        }

        // this is our parent bean, that's the classdef we start with
        if (dbPropClassDef == null) {
            try {
                dbPropClassDef = getBeanLoader().getClassOf(dbBean);
            } catch (NodeException e1) {
                e1.printStackTrace();
            }
        }
        
        getSite().setSelectionProvider(this);
    }

    @Override
    public boolean isDirty() {
        return pfe.isDirty() || pages.stream().anyMatch(page -> page.getDataNode().isModelModified());
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void setFocus() {
        if (getContainer() != null){
            getContainer().setFocus();
        }
    }

    @Override
    protected void createPages() {
        Composite ourContainer = this.getContainer();
        
        if ( toolkit == null){
            toolkit = new FormToolkit(ourContainer.getDisplay() );
            toolkit.setBackground(ourContainer.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
            toolkit.setBorderStyle(ourContainer.getBorderWidth());
        }
        
        ourContainer.addDisposeListener(e -> {
		    if (toolkit != null) {
		        toolkit.dispose();
		        toolkit = null;
		    }
		    getDBPropObject().release();
		});
        
        MainPage page1 = new MainPage(this.getContainer(), toolkit, this);
        this.pages.add(page1);
        addPage(page1);
        setPageText(GENERAL_TAB, "General");
        
        page1.getDataNode().setClassDef(getDBPropClassDef());
        SingleCollection col = new SingleCollection(dbBean);
        page1.getDataNode().setDataProvider(col);
        page1.getDataNode().addDataNodeListener(new DataNodeAdapter() {
            public void onModifiedChanged(DataNode source) {
                firePropertyChange(IEditorPart.PROP_DIRTY);
            }
        });
//        getDBPropObject().getXspProperties().addPropertyChangeListener(prop -> firePropertyChange(IEditorPart.PROP_DIRTY));
        page1.initProject();
        page1.getDataNode().setModelModified(false);
        
        // since we're about to add one, this is the index of the source tab
        sourcePageNum = this.getPageCount();     
        try {
            addPage(pfe, getDBPropInput());
            setPageText(sourcePageNum, "Source");
        } catch (PartInitException e) {
            e.printStackTrace();
        }
        setPartName(getTabLabel());
    }
    
    protected String getTabLabel() {
        String dbName = null;
        if (dp != null)
            dbName = dp.getDatabaseTitle();
        else
            dbName = project.getName();
        String partName =  StringUtil.format("{0} - {1}", getTitle(), dbName); //$NON-NLS-1$
        return partName; 
    }

    public DesignerProject getDominoDesignerProject() {
        IProject ourProject = getDesignerProject();
        if (ourProject != null) {
            dp = DesignerResource.getDesignerProject(ourProject);
            return dp;
        }
        else
            return null;
    }

    protected IProject getDesignerProject() {
        if (project == null) {
            if (dbPropInput != null) {
                project = dbPropInput.getFile().getProject();
                return project;
            }
        }
        return project;
    }

    public XSPPropBeanLoader getBeanLoader() {
        return jbl;
    }

    public FileEditorInput getDBPropInput() {
        return dbPropInput;
    }
    
    public JakartaPropBean getDBPropObject() {
        return dbBean;
    }

    public void addSelectionChangedListener(ISelectionChangedListener arg0) {
        
    }

    public ISelection getSelection() {
        if (editorSelection == null) {
            editorSelection = new StructuredSelection(getDominoDesignerProject());
        }
        return editorSelection;
    }

    public void removeSelectionChangedListener(ISelectionChangedListener arg0) {
    }

    public void setSelection(ISelection selection) {
        editorSelection = selection;
    }
    
    public void setModified(boolean isDirty) {
        firePropertyChange(IEditorPart.PROP_DIRTY);
    }

    public IClassDef getDBPropClassDef() {
        if (dbPropClassDef == null) {
            try {
                if (getBeanLoader() == null) {
                    jbl = new XSPPropBeanLoader("dbjbl"); //$NON-NLS-1$
                }
                dbPropClassDef = getBeanLoader().getClassOf(dbBean);
            } catch (NodeException e) {
            }
        }
        return dbPropClassDef;
    }
    
    public IClassDef getXSPDesignPropClassDef() {
        if (xspDesignPropsClassDef == null) {
            try {
                xspDesignPropsClassDef = xspDesignLoader.getClassOf(xspDesignPropsBean);
            } catch (NodeException e) {
            }
        }
        return xspDesignPropsClassDef;
    }
    
    public PropertiesFileEditor getPropertiesEditor() {
        return pfe;
    }

    protected void pageChange(int newPageIndex) {
        super.pageChange(newPageIndex);                     // do this so the delete key works right!!
        // if switching away from the source tab, update the other page data nodes
        if (curPage == sourcePageNum && newPageIndex != sourcePageNum) {
            String docContents = pfe.getDocumentProvider().getDocument(dbPropInput).get();
            Properties jProps = dbBean.getXspProperties().getPropertiesObj();
            byte[] bytes = docContents.getBytes(StandardCharsets.ISO_8859_1); // properties files have to have this encoding
            try(InputStream is = new ByteArrayInputStream(bytes)) {
                jProps.clear();
                jProps.load(is);
                this.pages.forEach(page -> page.getDataNode().notifyInvalidate(null));
            } catch (IOException e) {
            	e.printStackTrace();
            }
        }
        curPage = this.getActivePage();
    }

    @Override
    public void dispose() {
        jbl = null;
        xspDesignLoader = null;
        super.dispose();
    }
}