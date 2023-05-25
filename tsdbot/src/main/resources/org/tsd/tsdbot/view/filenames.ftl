<#-- @ftlvariable name="" type="org.tsd.tsdbot.view.FilenamesView" -->
<#import "layout.ftl" as layout>
<@layout.layout title="TSD Filenames Database">
    <h1>Filenames</h1>
    <div>
        <ul>
            <#list allFilenames as filename>
                <li>
                    <a href="filenames/${filename}" target="_blank">${filename}</a>
                </li>
            </#list>
        </ul>
    </div>
</@layout.layout>