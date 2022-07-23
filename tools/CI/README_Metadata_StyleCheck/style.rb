all  # Opt-in all rules by default
rule 'MD003', :style => :atx  # Header style written as non-closing pound marks. e.g. ## Section title
rule 'MD004', :style => :asterisk  # Unordered list style as asterisk, rather than hyphen or plus sign
rule 'MD009', :br_spaces => 2  # Allows an exception for 2 trailing spaces used to insert an explicit line break
rule 'MD029', :style => :ordered  # Ordered list item prefix is incremental, rather than all ones
exclude_rule 'MD013'  # Not limiting line length
exclude_rule 'MD007'  # Not limiting unordered list indentation, tab, 2 or 4 spaces are all fine