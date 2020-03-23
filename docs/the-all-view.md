## Why can't I edit the "All" view?

This is because it is of type "All" not of type "List", and the "All" type is
not editable. You can only have one view of type "All".

If you want to edit this view, you will have to create a new view instead. See
other questions on this page for more information.

## Why can't I delete the "All" view?

This is because it is set as your default view. To delete this view, go to
"Manage Jenkins" \> "Configure System" and change the selection in
the "Default View" drop-down.

You can't change the default view unless you already have another view created.
Once you have changed to a new default view, you can delete the "All" view.

## How do I delete the "All" view in "My views"?

To delete this view, go to "People" \> "*your username/userid*" \> "Configure"
and change the selection in the "Default View" text field (this should actually
be a drop-down too!).

Just like the global default view, you can't delete a user's default view
unless you already have another view created. Once you have changed to a new
default view, you can delete the user's "All" view.

## How can I create an "Editable All View"?

You might want a view like "All" that shows all jobs, but perhaps you want to
be able to change the columns around a bit. To do this, you need to create a
"faux-All" view, by following these steps.

1. Create your new view. Give it a name like "All2", and do not choose the type
   "All" if that option is available. Choose "List" or some other option if you
   have other plugins installed.
2. Find the checkbox labeled "Use a regular expression to include jobs into the
   view" and check it.
3. Enter the regular expression ".\*" to pick up all jobs (or use the [View Job
   Filters](https://plugins.jenkins.io/view-job-filters/#ViewJobFilters-ShowingAllJobswiththe%22AllJobs%22Filter))
4. Configure the view in any other way you like. For example, changing the
   columns you want to show.
5. Save the new view
6. Optionally, now you can replace your "All" view with your new view with the
   remaining steps
7. Follow the instructions above for [Deleting the All
   view](#why-cant-i-delete-the-all-view)
8. Rename your temporary "All2" view to "All" by clicking the "Edit View" link
   on the left of that view's page

